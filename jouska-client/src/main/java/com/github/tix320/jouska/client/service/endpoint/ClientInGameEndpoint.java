package com.github.tix320.jouska.client.service.endpoint;

import com.github.tix320.jouska.client.infrastructure.event.EventDispatcher;
import com.github.tix320.jouska.client.infrastructure.event.game.CanTurnEvent;
import com.github.tix320.jouska.client.infrastructure.event.game.ForceCompleteGameEvent;
import com.github.tix320.jouska.client.infrastructure.event.game.LeaveEvent;
import com.github.tix320.jouska.client.infrastructure.event.game.TurnEvent;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("in-game")
public class ClientInGameEndpoint {

	@Endpoint("canTurn")
	public void canTurn() {
		EventDispatcher.fire(new CanTurnEvent());
	}

	@Endpoint("turn")
	public void turn(Point point) {
		EventDispatcher.fire(new TurnEvent(point));
	}

	@Endpoint("leave")
	public void leave(Player player) {
		EventDispatcher.fire(new LeaveEvent(player));
	}

	@Endpoint("forceComplete")
	public void forceComplete(Player winner) {
		EventDispatcher.fire(new ForceCompleteGameEvent(winner));
	}
}

