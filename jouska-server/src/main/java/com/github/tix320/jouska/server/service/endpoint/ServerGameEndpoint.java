package com.github.tix320.jouska.server.service.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.server.service.application.GameManager;
import com.github.tix320.jouska.server.service.application.PlayerService;
import com.github.tix320.jouska.server.service.endpoint.authentication.NeedAuthentication;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscribe;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("game")
public class ServerGameEndpoint {

	@Endpoint("info")
	@Subscribe
	@NeedAuthentication
	public Observable<List<GameView>> getGames(@ClientID long clientId) {
		return GameManager.games().map(games -> games.stream().map(gameInfo -> {
			GameSettings settings = gameInfo.getSettings();
			return new GameView(gameInfo.getId(), settings.getName(), gameInfo.getConnectedPlayers().size(),
					settings.getPlayersCount(), settings.getTurnDurationSeconds(),
					settings.getGameDurationMinutes());
		}).collect(Collectors.toList()));
	}

	@Endpoint("connect")
	@NeedAuthentication
	public GameConnectionAnswer connect(long gameId, @ClientID long clientId) {
		return GameManager.connectToGame(gameId, PlayerService.getPlayerByClientId(clientId));
	}

	@Endpoint("create")
	@NeedAuthentication
	public long createGame(CreateGameCommand createGameCommand, @ClientID long clientId) {
		return GameManager.createNewGame(createGameCommand);
	}

	@Endpoint("watch")
	public void watchGame(long gameId, @ClientID long clientId) {
		GameManager.watchGame(gameId, clientId);
	}
}
