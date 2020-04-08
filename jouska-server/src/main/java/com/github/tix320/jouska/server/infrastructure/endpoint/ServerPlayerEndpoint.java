package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.List;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

/**
 * @author tigra on 05-Apr-20.
 */
@Endpoint("player")
public class ServerPlayerEndpoint {

	@Endpoint
	public List<Player> getPlayersByNickname(List<String> nicknames) {
		return PlayerService.findPlayersByNickname(nicknames);
	}
}
