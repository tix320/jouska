package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.app.inject.EndpointModule;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.UserExtraArgInjector;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.ravel.api.*;
import com.github.tix320.sonder.api.common.rpc.RPCProtocol;
import com.github.tix320.sonder.api.common.rpc.RPCProtocolBuilder;
import com.github.tix320.sonder.api.server.SonderServer;
import com.github.tix320.sonder.api.server.event.ClientConnectionClosedEvent;
import com.github.tix320.sonder.api.server.event.NewClientConnectionEvent;

public class AppConfig {
	private static SonderServer sonderServer;

	public static Injector INJECTOR;

	public static synchronized void initialize(int port) {
		if (sonderServer != null) {
			throw new IllegalStateException("Server already start");
		}

		Injector injector = new Injector();
		injector.registerModule(EndpointModule.class);

		RPCProtocolBuilder builder = RPCProtocol.forServer()
				.scanOriginPackages("com.github.tix320.jouska.server.infrastructure.origin")
				.processOriginInstances(originInstances -> {
					DynamicModuleDefinition dynamicOriginsModule = createDynamicOriginsModule(originInstances);
					injector.registerDynamicModule(dynamicOriginsModule);
				});

		injector.build();

		RPCProtocol protocol = builder.scanEndpointPackages(
				List.of("com.github.tix320.jouska.server.infrastructure.endpoint"), injector::inject)
				.registerEndpointExtraArgInjector(injector.inject(UserExtraArgInjector.class))
				.build();

		PlayerService playerService = injector.inject(PlayerService.class);
		ClientPlayerMappingResolver clientPlayerMappingResolver = injector.inject(ClientPlayerMappingResolver.class);

		sonderServer = SonderServer.forAddress(new InetSocketAddress(port))
				.registerProtocol(protocol)
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(10000))
				.build();

		sonderServer.onEvent(NewClientConnectionEvent.class).subscribe(newClientConnectionEvent -> {
			// System.out.println("Connected client: " + newClientConnectionEvent.getClientId());
		});

		sonderServer.onEvent(ClientConnectionClosedEvent.class).subscribe(event -> {
			long clientId = event.getClientId();
			String playerId = clientPlayerMappingResolver.removeByClientId(clientId);
			if (playerId != null) {
				Player player = playerService.getPlayerById(playerId).orElseThrow();
				EventDispatcher.fire(new PlayerDisconnectedEvent(player));
			}
		});

		INJECTOR = injector;

		try {
			sonderServer.start();
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot start server", e);
		}
	}

	public static synchronized void stop() throws IOException {
		if (sonderServer == null) {
			throw new IllegalStateException("Server does not started");
		}
		sonderServer.close();
	}

	private static DynamicModuleDefinition createDynamicOriginsModule(Map<Class<?>, Object> instances) {
		Map<BeanKey, BeanDefinition> beanDefinitions = new HashMap<>();
		for (Entry<Class<?>, Object> entry : instances.entrySet()) {
			Class<?> clazz = entry.getKey();
			Object instance = entry.getValue();
			BeanKey beanKey = new BeanKey(clazz);
			beanDefinitions.put(beanKey,
					new BeanDefinition(beanKey, Scope.SINGLETON, List.of(), dependencies -> instance));
		}

		return new DynamicModuleDefinition("origins", List.of(), beanDefinitions);
	}
}

