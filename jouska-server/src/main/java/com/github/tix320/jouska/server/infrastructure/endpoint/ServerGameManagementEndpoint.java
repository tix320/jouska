package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.dto.GameWatchDto;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Endpoint("game")
public class ServerGameManagementEndpoint {

	@Endpoint("info")
	@Subscription
	public Observable<List<GameView>> games(@CallerUser Player player) {
		return GameManager.games(player)
				.map(games -> games.stream()
						.map(gameInfo -> new GameView(gameInfo.getId(), (TimedGameSettings) gameInfo.getSettings(),
								gameInfo.getCreator(), gameInfo.getConnectedPlayers(),
								gameInfo.getGame().isPresent() && gameInfo.getGame().get().isStarted()))
						.collect(Collectors.toList()));
	}

	@Endpoint
	public GameConnectionAnswer join(long gameId, @CallerUser Player player) {
		return GameManager.joinGame(gameId, player);
	}

	@Endpoint
	public void leave(long gameId, @CallerUser Player player) {
		GameManager.leaveGame(gameId, player);
	}

	@Endpoint("create")
	public long createGame(CreateGameCommand createGameCommand, @CallerUser Player player) {
		return GameManager.createNewGame(createGameCommand, player);
	}

	@Endpoint("start")
	public void startGame(long gameId, @CallerUser Player player) {
		GameManager.startGame(gameId, player);
	}

	@Endpoint("watch")
	public GameWatchDto watchGame(long gameId) {
		return GameManager.watchGame(gameId);
	}
}
