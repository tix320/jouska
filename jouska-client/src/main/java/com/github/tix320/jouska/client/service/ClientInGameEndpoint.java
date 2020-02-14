package com.github.tix320.jouska.client.service;

import com.github.tix320.jouska.client.ui.game.GameController;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("in-game")
public class ClientInGameEndpoint {

	@Endpoint("turn")
	public void turn(Point point) {
		GameController.CURRENT.turn(point);
	}

	@Endpoint("leave")
	public void lose(Player player) {
		GameController.CURRENT.leave(player);
	}
}

