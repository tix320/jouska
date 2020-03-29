package com.github.tix320.jouska.bot;

import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.jouska.core.dto.LoginResult;
import com.github.tix320.sonder.api.client.SonderClient;

public class BotApp {

	public static SonderClient SONDER_CLIENT;

	public static void main(String[] args) {
		String host = "localhost";//args[0];
		int port = 8888;//Integer.parseInt(args[1]);
		SONDER_CLIENT = SonderClient.forAddress(new InetSocketAddress(host, port))
				.withRPCProtocol(builder -> builder.scanPackages("com.github.tix320.jouska.bot"))
				.headersTimeoutDuration(Duration.ofSeconds(Integer.MAX_VALUE))
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(Integer.MAX_VALUE))
				.build();

		BotGameService botGameService = SONDER_CLIENT.getRPCService(BotGameService.class);

		AuthenticationService authenticationService = SONDER_CLIENT.getRPCService(AuthenticationService.class);

		authenticationService.forceLogin(new LoginCommand("Bot", "bot")).subscribe(loginAnswer -> {
			if (loginAnswer.getLoginResult() == LoginResult.SUCCESS) {
				botGameService.games().toMono().subscribe(gameViews -> {
					GameView gameView = gameViews.get(0);
					long id = gameView.getId();
					botGameService.connect(id);
				});
			}
			else {
				throw new IllegalStateException(loginAnswer.toString());
			}
		});
	}
}
