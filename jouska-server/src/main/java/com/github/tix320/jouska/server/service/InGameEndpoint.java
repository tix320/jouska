package com.github.tix320.jouska.server.service;

import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("in-game")
public class InGameEndpoint {

	@Endpoint("turn")
	public void turn(long gameId, Point point, @ClientID long clientId) {
		GameManager.turnInGame(gameId, clientId, point);
	}
}
