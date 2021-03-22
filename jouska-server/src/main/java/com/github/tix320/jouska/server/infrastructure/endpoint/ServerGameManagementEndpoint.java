package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.TournamentDao;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.entity.GameEntity;
import com.github.tix320.jouska.server.infrastructure.entity.PlayOffGameEntity;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import com.github.tix320.jouska.server.infrastructure.entity.TournamentEntity;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import org.bson.types.ObjectId;

@Endpoint("game")
public class ServerGameManagementEndpoint {

	private final GameDao gameDao;

	private final TournamentDao tournamentDao;

	private final GameManager gameManager;

	public ServerGameManagementEndpoint(GameDao gameDao, TournamentDao tournamentDao, GameManager gameManager) {
		this.gameDao = gameDao;
		this.tournamentDao = tournamentDao;
		this.gameManager = gameManager;
	}

	@Endpoint("info")
	@Subscription
	public Observable<List<GameView>> games(GameListFilter gameListFilter, @CallerUser Player player) {
		return gameManager.games(player)
				.map(games -> games.stream()
						.filter(game -> gameListFilter.getStates().contains(game.getState()))
						.map(game -> new GameView(game.getId(), GameSettingsDto.fromModel(game.getSettings()),
								game.getCreator(), game.getPlayers(), game.getState()))
						.collect(Collectors.toList()))
				.map(gameViews -> {
					List<ObjectId> ids = gameViews.stream()
							.map(GameView::getId)
							.map(ObjectId::new)
							.collect(Collectors.toList());
					if (gameListFilter.getStates().contains(GameState.COMPLETED)) {
						Filter filter = ids.isEmpty() ? Filters.eq("state", GameState.COMPLETED) :
								Filters.and(Filters.eq("state", GameState.COMPLETED), Filters.nin("_id", ids));
						List<GameView> completedGames = gameDao.findAll(filter, "settings", "state", "creator")
								.stream()
								.map(gameEntity -> {
									PlayerEntity creator = gameEntity.getCreator();
									return new GameView(gameEntity.getId(),
											GameSettingsDto.fromModel(gameEntity.getSettings()),
											new Player(creator.getId(), creator.getNickname(), creator.getRole()),
											Collections.emptyList(), gameEntity.getState());
								})
								.collect(Collectors.toList());

						gameViews.addAll(completedGames);
					}

					return gameViews;
				})
				.map(gameViews -> {
					String tournamentId = gameListFilter.getTournamentId();
					if (tournamentId == null) {
						return gameViews;
					}

					TournamentEntity tournamentEntity = tournamentDao.findById(tournamentId).orElseThrow();
					List<String> groupGameIds = tournamentEntity.getGroups()
							.stream()
							.flatMap(groupEntity -> groupEntity.getGames().stream().map(GameEntity::getId))
							.collect(Collectors.toList());
					List<String> playOffGameIds = Optional.ofNullable(tournamentEntity.getPlayOff())
							.map(playOffEntity -> playOffEntity.getTours()
									.stream()
									.flatMap(Collection::stream)
									.map(PlayOffGameEntity::getGame)
									.map(GameEntity::getId)
									.collect(Collectors.toList()))
							.orElse(Collections.emptyList());

					Set<String> idsToFilter = new HashSet<>(groupGameIds);
					idsToFilter.addAll(playOffGameIds);

					return gameViews.stream()
							.filter(gameView -> idsToFilter.contains(gameView.getId()))
							.collect(Collectors.toList());
				});
	}

	@Endpoint
	public GameConnectionAnswer join(String gameId, @CallerUser Player player) {
		Objects.requireNonNull(gameId, "Game id not specified");
		return gameManager.joinGame(gameId, player);
	}

	@Endpoint
	public void leave(String gameId, @CallerUser Player player) {
		gameManager.leaveGame(gameId, player);
	}

	@Endpoint("create")
	public String createGame(CreateGameCommand createGameCommand, @CallerUser Player player) {
		return gameManager.createGame(createGameCommand.getGameSettings().toModel(), player,
				createGameCommand.getAccessedPlayers());
	}

	@Endpoint("start")
	public void startGame(String gameId, @CallerUser Player player) {
		gameManager.startGame(gameId, player);
	}

	@Endpoint("watch")
	public GameWatchDto watchGame(String gameId) {
		return gameManager.watchGame(gameId);
	}
}
