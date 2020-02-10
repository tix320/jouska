package com.github.tix320.jouska.bot;

import java.net.InetSocketAddress;
import java.time.Duration;

import com.github.tix320.sonder.api.client.Clonder;

public class BotApp {

	public static Clonder CLONDER;

	public static void main(String[] args) {
		String host = "localhost";//args[0];
		int port = 8888;//Integer.parseInt(args[1]);
		int gameId = 1;
		CLONDER = Clonder.forAddress(new InetSocketAddress(host, port))
				.withRPCProtocol("com.github.tix320.jouska.bot")
				.withTopicProtocol()
				.headersTimeoutDuration(Duration.ofSeconds(Integer.MAX_VALUE))
				.contentTimeoutDurationFactory(contentLength -> {
					return Duration.ofSeconds(Integer.MAX_VALUE);
				})
				.build();
		CLONDER.getRPCService(BotGameService.class).connect(gameId);
	}
}
