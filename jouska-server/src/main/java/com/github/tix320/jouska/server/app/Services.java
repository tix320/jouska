package com.github.tix320.jouska.server.app;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.service.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.service.application.PlayerService;
import com.github.tix320.jouska.server.service.endpoint.auth.UserExtraArgExtractor;
import com.github.tix320.jouska.server.service.origin.AuthenticationService;
import com.github.tix320.jouska.server.service.origin.ServerGameService;
import com.github.tix320.sonder.api.server.SonderServer;
import com.github.tix320.sonder.api.server.event.ClientConnectionClosedEvent;

public class Services {
	public static SonderServer SONDER_SERVER;
	public static AuthenticationService AUTHENTICATION_SERVICE;
	public static ServerGameService GAME_SERVICE;

	public static void initialize(int port) {
		if (SONDER_SERVER != null) {
			throw new IllegalStateException("Server already start, maybe you wish reconnect?");
		}
		String servicesPackage = "com.github.tix320.jouska.server.service";
		SONDER_SERVER = SonderServer.forAddress(new InetSocketAddress(port))
				.withRPCProtocol(builder -> builder.scanPackages(servicesPackage)
						.registerEndpointExtraArgExtractor(new UserExtraArgExtractor()))
				.withTopicProtocol()
				.headersTimeoutDuration(Duration.ofSeconds(Integer.MAX_VALUE))
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(Integer.MAX_VALUE))
				.build();
		initServices();


		SONDER_SERVER.onEvent(ClientConnectionClosedEvent.class).subscribe(event -> {
			long clientId = event.getClientId();
			String playerId = ClientPlayerMappingResolver.removeByClientId(clientId);
			if (playerId != null) {
				Player player = PlayerService.findPlayerById(playerId).orElseThrow();
				EventDispatcher.fire(new PlayerDisconnectedEvent(player));
			}
		});
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
	}
}

