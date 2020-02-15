package com.github.tix320.jouska.client.service;

import com.github.tix320.jouska.client.app.JouskaUI;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class ClientGameEndpoint {

	@Endpoint("start")
	public void startGame(StartGameCommand startGameCommand) {
		JouskaUI.switchScene("game", startGameCommand);
	}

	@Endpoint("watch")
	public void watchGame(WatchGameCommand watchGameCommand) {
		JouskaUI.switchScene("game-watching", watchGameCommand);
	}

}