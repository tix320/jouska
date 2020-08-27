package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.tix320.jouska.client.app.inject.ControllersModule;
import com.github.tix320.jouska.client.app.inject.EndpointModule;
import com.github.tix320.ravel.api.*;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.common.rpc.RPCProtocol;
import com.github.tix320.sonder.api.common.rpc.RPCProtocolBuilder;

public class AppConfig {
	private static SonderClient sonderClient;

	public static Injector INJECTOR;

	public static synchronized void initialize(String host, int port) {
		if (sonderClient != null) {
			throw new IllegalStateException("Server already start");
		}

		Injector injector = new Injector();
		injector.registerModule(EndpointModule.class);
		injector.registerModule(ControllersModule.class);

		RPCProtocolBuilder builder = RPCProtocol.forClient()
				.scanOriginPackages("com.github.tix320.jouska.client.service.origin")
				.processOriginInstances(originInstances -> {
					DynamicModuleDefinition dynamicOriginsModule = createDynamicOriginsModule(originInstances);
					injector.registerDynamicModule(dynamicOriginsModule);
				});

		injector.build();

		RPCProtocol protocol = builder.scanEndpointPackages(List.of("com.github.tix320.jouska.client.service.endpoint"),
				injector::inject).build();

		sonderClient = SonderClient.forAddress(new InetSocketAddress(host, port))
				.registerProtocol(protocol)
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(10000))
				.build();

		INJECTOR = injector;
	}

	public static synchronized void connect() {
		if (sonderClient == null) {
			throw new IllegalStateException();
		}

		try {
			sonderClient.connect();
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot connect to server", e);
		}
	}

	public static synchronized void stop() throws IOException {
		if (sonderClient == null) {
			throw new IllegalStateException("Not connected yet");
		}
		sonderClient.close();
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

