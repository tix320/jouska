package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.ClassUtils;
import com.github.tix320.jouska.server.app.inject.CommonModule;
import com.github.tix320.jouska.server.app.inject.EndpointModule;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.UserExtraArgInjector;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.ravel.api.Injector;
import com.github.tix320.ravel.api.bean.BeanDefinition;
import com.github.tix320.ravel.api.bean.BeanKey;
import com.github.tix320.ravel.api.module.DynamicModuleDefinition;
import com.github.tix320.ravel.api.scope.Scope;
import com.github.tix320.sonder.api.server.SonderServer;
import com.github.tix320.sonder.api.server.event.ClientConnectionClosedEvent;
import com.github.tix320.sonder.api.server.event.NewClientConnectionEvent;
import com.github.tix320.sonder.api.server.rpc.ServerRPCProtocol;
import com.github.tix320.sonder.api.server.rpc.ServerRPCProtocolBuilder;

public class AppConfig {
	private static SonderServer sonderServer;

	public static Injector INJECTOR;

	public static synchronized void initialize() {
		if (sonderServer != null) {
			throw new IllegalStateException("Server already start");
		}

		Injector injector = new Injector();
		injector.registerModule(CommonModule.class);
		injector.registerModule(EndpointModule.class);

		Class<?>[] originInterfaces = ClassUtils.getPackageClasses(
				"com.github.tix320.jouska.server.infrastructure.origin");

		ServerRPCProtocolBuilder builder = SonderServer.getRPCProtocolBuilder()
				.registerOriginInterfaces(originInterfaces)
				.processOriginInstances(originInstances -> {
					DynamicModuleDefinition dynamicOriginsModule = createDynamicOriginsModule(originInstances);
					injector.registerDynamicModule(dynamicOriginsModule);
				});

		injector.build();

		Class<?>[] endpointClasses = ClassUtils.getPackageClasses(
				"com.github.tix320.jouska.server.infrastructure.endpoint");
		List<Object> endpointInstances = Arrays.stream(endpointClasses)
				.map(injector::inject)
				.collect(Collectors.toList());

		ServerRPCProtocol protocol = builder.registerEndpointInstances(endpointInstances)
				.registerEndpointExtraArgInjector(injector.inject(UserExtraArgInjector.class))
				.build();

		PlayerService playerService = injector.inject(PlayerService.class);
		ClientPlayerMappingResolver clientPlayerMappingResolver = injector.inject(ClientPlayerMappingResolver.class);

		Configuration configuration = AppConfig.INJECTOR.inject(Configuration.class);

		sonderServer = SonderServer.forAddress(new InetSocketAddress(configuration.getPort()))
				.registerProtocol(protocol)
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(10000))
				.build();

		sonderServer.getEventListener()
				.on(NewClientConnectionEvent.class)
				.subscribe(newClientConnectionEvent -> System.out.println(
						"Connected client: " + newClientConnectionEvent.getClientId()));

		sonderServer.getEventListener().on(ClientConnectionClosedEvent.class).subscribe(event -> {
			long clientId = event.getClientId();
			String playerId = clientPlayerMappingResolver.removeByClientId(clientId);
			if (playerId != null) {
				Player player = playerService.getPlayerById(playerId).orElseThrow();
				EventDispatcher.fire(new PlayerDisconnectedEvent(player));
			}
		});

		INJECTOR = injector;
	}

	public static synchronized void start() throws IOException {
		if (sonderServer == null) {
			throw new IllegalStateException();
		}

		sonderServer.start();
	}

	public static synchronized void stop() throws IOException {
		if (sonderServer == null) {
			throw new IllegalStateException("Server does not started");
		}
		sonderServer.stop();
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

