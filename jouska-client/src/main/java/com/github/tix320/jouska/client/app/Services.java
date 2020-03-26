package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.client.service.origin.*;
import com.github.tix320.sonder.api.client.SonderClient;

public class Services {
	public static AuthenticationService AUTHENTICATION_SERVICE;
	public static ClientGameManagmentService GAME_SERVICE;
	public static ClientGameService IN_GAME_SERVICE;
	public static ClientTournamentService TOURNAMENT_SERVICE;
	public static ApplicationUpdateService APPLICATION_INSTALLER_SERVICE;

	public static SonderClient SONDER_CLIENT;

	public static void initialize(String host, int port) {
		if (SONDER_CLIENT != null) {
			throw new IllegalStateException("Client already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.github.tix320.jouska.client.service";
		SONDER_CLIENT = SonderClient.forAddress(new InetSocketAddress(host, port))
				.withRPCProtocol(builder -> builder.scanPackages(servicesPackage))
				.withTopicProtocol()
				.headersTimeoutDuration(Duration.ofSeconds(Integer.MAX_VALUE))
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(Integer.MAX_VALUE))
				.build();
		initServices();
	}

	public static void stop()
			throws IOException {
		if (SONDER_CLIENT != null) {
			SONDER_CLIENT.close();
			SONDER_CLIENT = null;
		}
	}

	private static void initServices() {
		AUTHENTICATION_SERVICE = SONDER_CLIENT.getRPCService(AuthenticationService.class);
		GAME_SERVICE = SONDER_CLIENT.getRPCService(ClientGameManagmentService.class);
		IN_GAME_SERVICE = SONDER_CLIENT.getRPCService(ClientGameService.class);
		TOURNAMENT_SERVICE = SONDER_CLIENT.getRPCService(ClientTournamentService.class);
		APPLICATION_INSTALLER_SERVICE = SONDER_CLIENT.getRPCService(ApplicationUpdateService.class);
	}
}
