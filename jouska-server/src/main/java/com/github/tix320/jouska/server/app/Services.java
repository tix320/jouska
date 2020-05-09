package com.github.tix320.jouska.server.app;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.UserExtraArgExtractor;
import com.github.tix320.jouska.server.infrastructure.origin.AuthenticationOrigin;
import com.github.tix320.jouska.server.infrastructure.origin.ServerGameManagementOrigin;
import com.github.tix320.jouska.server.infrastructure.origin.ServerTournamentOrigin;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.sonder.api.server.SonderServer;
import com.github.tix320.sonder.api.server.event.ClientConnectionClosedEvent;
import com.github.tix320.sonder.api.server.event.NewClientConnectionEvent;

public class Services {
	public static SonderServer SONDER_SERVER;
	public static AuthenticationOrigin AUTHENTICATION_ORIGIN;
	public static ServerGameManagementOrigin GAME_ORIGIN;
	public static ServerTournamentOrigin TOURNAMENT_ORIGIN;

	private static final PlayerService playerService = new PlayerService();

	public static void initialize(int port) {
		if (SONDER_SERVER != null) {
			throw new IllegalStateException("Server already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.github.tix320.jouska.server.infrastructure";
		SONDER_SERVER = SonderServer.forAddress(new InetSocketAddress(port))
				.withRPCProtocol(builder -> builder.scanPackages(servicesPackage)
						.registerEndpointExtraArgExtractor(new UserExtraArgExtractor()))
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(100))
				.build();
		initServices();

		SONDER_SERVER.onEvent(NewClientConnectionEvent.class).subscribe(newClientConnectionEvent -> {
			System.out.println("Connected client: " + newClientConnectionEvent.getClientId());
		});

		SONDER_SERVER.onEvent(ClientConnectionClosedEvent.class).subscribe(event -> {
			long clientId = event.getClientId();
			String playerId = ClientPlayerMappingResolver.removeByClientId(clientId);
			if (playerId != null) {
				Player player = playerService.getPlayerById(playerId).orElseThrow();
				EventDispatcher.fire(new PlayerDisconnectedEvent(player));
			}
		});

		try {
			SONDER_SERVER.start();
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot start server", e);
		}
	}

	public static void stop() throws IOException {
		if (SONDER_SERVER == null) {
			throw new IllegalStateException("Server does not started");
		}
		SONDER_SERVER.close();
	}

	private static void initServices() {
		AUTHENTICATION_ORIGIN = SONDER_SERVER.getRPCService(AuthenticationOrigin.class);
		GAME_ORIGIN = SONDER_SERVER.getRPCService(ServerGameManagementOrigin.class);
		TOURNAMENT_ORIGIN = SONDER_SERVER.getRPCService(ServerTournamentOrigin.class);
	}
}

