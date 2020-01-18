package com.github.tix320.jouska.server.app;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.server.service.GameService;
import com.github.tix320.sonder.api.server.Sonder;

public class Services {
	public static GameService GAME_SERVICE;
	public static Sonder SONDER;

	public static void initialize(int port) {
		if (SONDER != null) {
			throw new IllegalStateException("Server already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.github.tix320.jouska.server.service";
		SONDER = Sonder.forAddress(new InetSocketAddress(port))
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
		if (SONDER == null) {
			throw new IllegalStateException("Server does not started");
		}
		SONDER.close();
	}

	private static void initServices() {
		GAME_SERVICE = SONDER.getRPCService(GameService.class);
	}
}

