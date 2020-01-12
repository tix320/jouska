package com.gitlab.tixtix320.jouska.server.app;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.gitlab.tixtix320.jouska.server.service.GameService;
import com.gitlab.tixtix320.sonder.api.server.Sonder;

public class Services {
	public static GameService GAME_SERVICE;
	public static Sonder SONDER;

	public static void initialize(int port) {
		if (SONDER != null) {
			throw new IllegalStateException("Server already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.gitlab.tixtix320.jouska.server.service";
		SONDER = Sonder.forAddress(new InetSocketAddress(port))
				.withRPCProtocol(servicesPackage)
				.withTopicProtocol()
				.headersTimeoutDuration(Duration.ofSeconds(637216731L))
				.contentTimeoutDurationFactory(contentLength -> {
					long timout = Math.max((long) Math.ceil(contentLength * (60D / 1024 / 1024 / 1024)), 1);
					return Duration.ofSeconds(100000000000L);
				})
				.build();
		initServices();
	}

	public static void stop()
			throws IOException {
		if (SONDER == null) {
			throw new IllegalStateException("Server does not started");
		}
		SONDER.close();
	}

	private static void initServices() {
		GAME_SERVICE = SONDER.getRPCService(GameService.class);
	}
}

