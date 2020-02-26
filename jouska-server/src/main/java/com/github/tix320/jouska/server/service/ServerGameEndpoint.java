package com.github.tix320.jouska.server.service;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("game")
public class ServerGameEndpoint {

	@Endpoint("info")
	public List<GameView> getGames() {
		return GameManager.getGames()
				.stream()
				.map(gameInfo -> new GameView(gameInfo.getId(), gameInfo.getName(), gameInfo.getPlayerIds().size(),
						gameInfo.getPlayers().length, gameInfo.getTurnDurationSeconds(), gameInfo.getGameDurationMinutes()))
				.collect(Collectors.toList());
	}

	@Endpoint("connect")
	public GameConnectionAnswer connect(long gameId, @ClientID long clientId) {
		return GameManager.connectToGame(gameId, clientId);
	}

	@Endpoint("create")
	public long createGame(CreateGameCommand createGameCommand, @ClientID long clientId) {
		return GameManager.createNewGame(createGameCommand);
	}

	@Endpoint("watch")
	public void watchGame(long gameId, @ClientID long clientId) {
		GameManager.watchGame(gameId, clientId);
	}
}
