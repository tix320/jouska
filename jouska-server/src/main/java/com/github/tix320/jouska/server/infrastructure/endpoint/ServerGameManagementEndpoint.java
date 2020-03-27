package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("game")
public class ServerGameManagementEndpoint {

	@Endpoint("info")
	@Subscription
	public Observable<List<GameView>> games(@CallerUser Player player) {
		return GameManager.games().map(games -> games.stream().map(gameInfo -> {
			TimedGameSettings settings = (TimedGameSettings) gameInfo.getSettings();
			return new GameView(gameInfo.getId(), settings.getName(), gameInfo.getConnectedPlayers().size(),
					settings.getPlayersCount(), settings.getTurnDurationSeconds(), settings.getGameDurationMinutes());
		}).collect(Collectors.toList()));
	}

	@Endpoint("connect")
	public GameConnectionAnswer connect(long gameId, @CallerUser Player player) {
		return GameManager.connectToGame(gameId, player);
	}

	@Endpoint("create")
	public long createGame(CreateGameCommand createGameCommand, @CallerUser Player player) {
		return GameManager.createNewGame(createGameCommand);
	}

	@Endpoint("watch")
	public void watchGame(long gameId, @ClientID long clientId) {
		GameManager.watchGame(gameId, clientId);
	}
}