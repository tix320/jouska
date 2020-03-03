package com.github.tix320.jouska.client.service;

import com.github.tix320.jouska.client.ui.controller.GameController;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("in-game")
public class ClientInGameEndpoint {

	@Endpoint("canTurn")
	public void canTurn() {
		GameController.CURRENT.canTurn();
	}

	@Endpoint("turn")
	public void turn(Point point) {
		GameController.CURRENT.turn(point);
	}

	@Endpoint("leave")
	public void leave(Player player) {
		GameController.CURRENT.leave(player);
	}

	@Endpoint("forceComplete")
	public void forceComplete(Player winner) {
		GameController.CURRENT.forceComplete(winner);
	}
}

