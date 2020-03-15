package com.github.tix320.jouska.server.app;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.server.service.endpoint.authentication.AuthenticationInterceptor;
import com.github.tix320.jouska.server.service.endpoint.role.AuthorizationInterceptor;
import com.github.tix320.jouska.server.service.origin.AuthenticationService;
import com.github.tix320.jouska.server.service.origin.ServerGameService;
import com.github.tix320.jouska.server.service.origin.ServerInGameService;
import com.github.tix320.sonder.api.server.SonderServer;

public class Services {
	public static SonderServer SONDER_SERVER;
	public static AuthenticationService AUTHENTICATION_SERVICE;
	public static ServerGameService GAME_SERVICE;
	public static ServerInGameService IN_GAME_SERVICE;

	public static void initialize(int port) {
		if (SONDER_SERVER != null) {
			throw new IllegalStateException("Server already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.github.tix320.jouska.server.service";
		SONDER_SERVER = SonderServer.forAddress(new InetSocketAddress(port))
				.withRPCProtocol(builder -> builder.scanPackages(servicesPackage)
						.registerInterceptor(new AuthenticationInterceptor(), new AuthorizationInterceptor()))
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
		if (SONDER_SERVER == null) {
			throw new IllegalStateException("Server does not started");
		}
		SONDER_SERVER.close();
	}

	private static void initServices() {
		AUTHENTICATION_SERVICE = SONDER_SERVER.getRPCService(AuthenticationService.class);
		GAME_SERVICE = SONDER_SERVER.getRPCService(ServerGameService.class);
		IN_GAME_SERVICE = SONDER_SERVER.getRPCService(ServerInGameService.class);
	}
}

