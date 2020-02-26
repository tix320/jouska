package com.github.tix320.jouska.client.service;

import com.github.tix320.jouska.client.infrastructure.ControllerCommunicator;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class ClientGameEndpoint {

	@Endpoint("start")
	public void startGame(StartGameCommand startGameCommand) {
		ControllerCommunicator.publishStartGameCommand(startGameCommand);
	}

	@Endpoint("watch")
	public void watchGame(WatchGameCommand watchGameCommand) {
		// JouskaUI.switchScene("game-watching", watchGameCommand);
	}

}
