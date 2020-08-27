package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

/**
 * @author tigra on 05-Apr-20.
 */
@Endpoint("player")
public class ServerPlayerEndpoint {

	private final PlayerDao playerDao;

	public ServerPlayerEndpoint(PlayerDao playerDao) {
		this.playerDao = playerDao;
	}

	@Endpoint
	public List<Player> getPlayersByNickname(List<String> nicknames) {
		return playerDao.findPlayersByNickname(nicknames)
				.stream()
				.map(PlayerService::convertEntityToModel)
				.collect(Collectors.toList());
	}
}
