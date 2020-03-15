package com.github.tix320.jouska.server.service.endpoint;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.service.application.PlayerService;
import com.github.tix320.jouska.server.service.endpoint.authentication.NeedAuthentication;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("player")
public class PlayerEndpoint {

	@Endpoint("me")
	@NeedAuthentication
	public Player me(@ClientID long clientId) {
		return PlayerService.getPlayerByClientId(clientId);
	}
}
