package com.github.tix320.jouska.client.service;

import com.github.tix320.jouska.client.app.Jouska;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class GameEndpoint {

	@Endpoint("start")
	public void startGame(StartGameCommand startGameCommand) {
		Jouska.switchScene("game", startGameCommand);
	}

	@Endpoint("watch")
	public void watchGame(WatchGameCommand watchGameCommand) {
		Jouska.switchScene("game-watching", watchGameCommand);
	}

}
