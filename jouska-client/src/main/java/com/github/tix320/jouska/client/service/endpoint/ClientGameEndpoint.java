package com.github.tix320.jouska.client.service.endpoint;

import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class ClientGameEndpoint {

	@Endpoint
	public void notifyGameStarted(GamePlayDto gamePlayDto) {
		EventDispatcher.fire(new GameStartedEvent(gamePlayDto));
	}
}
