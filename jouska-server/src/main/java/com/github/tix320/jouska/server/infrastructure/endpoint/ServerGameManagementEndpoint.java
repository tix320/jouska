package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.dto.GameWatchDto;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.entity.PlayerEntity;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.service.GameService;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Endpoint("game")
public class ServerGameManagementEndpoint {

	@Endpoint("info")
	@Subscription
	public Observable<List<GameView>> games(@CallerUser Player player) {
		return GameManager.games(player).map(games -> games.stream().map(gameInfo -> {
			boolean isStarted = gameInfo.getGame().isPresent() && gameInfo.getGame().get().isStarted();
			return new GameView(gameInfo.getId(), (TimedGameSettings) gameInfo.getSettings(), gameInfo.getCreator(),
					gameInfo.getConnectedPlayers(), isStarted ? GameState.STARTED : GameState.INITIAL);
		}).collect(Collectors.toList())).map(gameViews -> {
			List<GameView> completedGames = GameService.getGames(List.of("settings", "state", "creator"),
					Map.of("state", "COMPLETED")).stream().map(gameEntity -> {
				PlayerEntity creator = gameEntity.getCreator();
				return new GameView(gameEntity.getId(), (TimedGameSettings) gameEntity.getSettings(),
						new Player(creator.getId().toHexString(), creator.getNickname(), creator.getRole()),
						Collections.emptySet(), gameEntity.getState());
			}).collect(Collectors.toList());

			gameViews.addAll(completedGames);
			return gameViews;
		});
	}

	@Endpoint
	public GameConnectionAnswer join(String gameId, @CallerUser Player player) {
		return GameManager.joinGame(gameId, player);
	}

	@Endpoint
	public void leave(String gameId, @CallerUser Player player) {
		GameManager.leaveGame(gameId, player);
	}

	@Endpoint("create")
	public String createGame(CreateGameCommand createGameCommand, @CallerUser Player player) {
		return GameManager.createNewGame(createGameCommand, player);
	}

	@Endpoint("start")
	public void startGame(String gameId, @CallerUser Player player) {
		GameManager.startGame(gameId, player);
	}

	@Endpoint("watch")
	public GameWatchDto watchGame(String gameId) {
		return GameManager.watchGame(gameId);
	}
}
