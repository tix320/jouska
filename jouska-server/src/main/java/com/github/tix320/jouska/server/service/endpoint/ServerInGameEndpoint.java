package com.github.tix320.jouska.server.service.endpoint;

import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.jouska.server.service.application.GameManager;
import com.github.tix320.jouska.server.service.application.PlayerService;
import com.github.tix320.jouska.server.service.endpoint.authentication.NeedAuthentication;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("in-game")
public class ServerInGameEndpoint {

	@Endpoint("turn")
	@NeedAuthentication
	public void turn(long gameId, Point point, @ClientID long clientId) {
		GameManager.turnInGame(gameId, PlayerService.getPlayerByClientId(clientId), point);
	}

	@Endpoint("leave")
	@NeedAuthentication
	public void leave(long gameId, @ClientID long clientId) {
		GameManager.leaveFromGame(gameId,PlayerService.getPlayerByClientId(clientId));
	}
}
