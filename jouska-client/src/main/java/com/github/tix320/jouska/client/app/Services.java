package com.github.tix320.jouska.client.app;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.client.service.GameService;
import com.github.tix320.jouska.client.service.ApplicationSourcesService;
import com.github.tix320.sonder.api.client.Clonder;

public class Services {
	public static GameService GAME_SERVICE;
	public static ApplicationSourcesService APPLICATION_INSTALLER_SERVICE;

	public static Clonder CLONDER;

	public static void initialize(String host, int port) {
		if (CLONDER != null) {
			throw new IllegalStateException("Client already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.github.tix320.jouska.client.service";
		CLONDER = Clonder.forAddress(new InetSocketAddress(host, port))
				.withRPCProtocol(servicesPackage)
				.withTopicProtocol()
				.headersTimeoutDuration(Duration.ofSeconds(Integer.MAX_VALUE))
				.contentTimeoutDurationFactory(contentLength -> {
					// long timout = Math.max((long) Math.ceil(contentLength * (500D / 1024 / 1024 / 30)), 1);
					return Duration.ofSeconds(Integer.MAX_VALUE);
				})
				.build();
		initServices();
	}

	public static void stop()
			throws IOException {
		if (CLONDER == null) {
			throw new IllegalStateException("Client does not started");
		}
		CLONDER.close();
	}

	private static void initServices() {
		GAME_SERVICE = CLONDER.getRPCService(GameService.class);
		APPLICATION_INSTALLER_SERVICE = CLONDER.getRPCService(ApplicationSourcesService.class);
	}
}
