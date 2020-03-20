package com.github.tix320.jouska.client.service.endpoint;

import com.github.tix320.jouska.client.infrastructure.event.EventDispatcher;
import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class ClientGameEndpoint {

	@Endpoint("start")
	public void startGame(StartGameCommand startGameCommand) {
		EventDispatcher.fire(new GameStartedEvent(startGameCommand));
	}

	@Endpoint("watch")
	public void watchGame(WatchGameCommand watchGameCommand) {
		// JouskaUI.switchScene("game-watching", watchGameCommand);
	}

}
