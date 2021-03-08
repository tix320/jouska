package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.app.inject.CommonModule;
import com.github.tix320.jouska.client.app.inject.ControllersModule;
import com.github.tix320.jouska.client.app.inject.EndpointModule;
import com.github.tix320.jouska.core.util.ClassUtils;
import com.github.tix320.ravel.api.Injector;
import com.github.tix320.ravel.api.bean.BeanDefinition;
import com.github.tix320.ravel.api.bean.BeanKey;
import com.github.tix320.ravel.api.module.DynamicModuleDefinition;
import com.github.tix320.ravel.api.scope.Scope;
import com.github.tix320.skimp.api.interval.Interval;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.client.rpc.ClientRPCProtocol;
import com.github.tix320.sonder.api.client.rpc.ClientRPCProtocolBuilder;

public class AppConfig {
	public static SonderClient sonderClient;

	public static Injector INJECTOR;

	public static synchronized void initialize() {
		if (sonderClient != null) {
			throw new IllegalStateException("Server already start");
		}

		Injector injector = new Injector();
		injector.registerModule(CommonModule.class);
		injector.registerModule(EndpointModule.class);
		injector.registerModule(ControllersModule.class);

		Class<?>[] originInterfaces = ClassUtils.getPackageClasses("com.github.tix320.jouska.client.service.origin");

		ClientRPCProtocolBuilder builder = ClientRPCProtocol.builder()
				.registerOriginInterfaces(originInterfaces)
				.processOriginInstances(originInstances -> {
					DynamicModuleDefinition dynamicOriginsModule = createDynamicOriginsModule(originInstances);
					injector.registerDynamicModule(dynamicOriginsModule);
				});

		injector.build();

		Class<?>[] endpointClasses = ClassUtils.getPackageClasses("com.github.tix320.jouska.client.service.endpoint");
		List<Object> endpointInstances = Arrays.stream(endpointClasses)
				.map(injector::inject)
				.collect(Collectors.toList());

		ClientRPCProtocol protocol = builder.registerEndpointInstances(endpointInstances).build();

		Configuration configuration = injector.inject(Configuration.class);

		sonderClient = SonderClient.forAddress(configuration.getServerAddress())
				.registerProtocol(protocol)
				.autoReconnect(Interval.raising(Duration.ZERO, Duration.ofSeconds(5)))
				.build();

		INJECTOR = injector;
	}

	public static synchronized void connectToServer() throws IOException {
		if (sonderClient == null) {
			throw new IllegalStateException();
		}

		sonderClient.start();
	}

	public static synchronized void stop() throws IOException {
		if (sonderClient == null) {
			throw new IllegalStateException("Not connected yet");
		}
		sonderClient.stop();
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

