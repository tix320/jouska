package com.github.tix320.jouska.server.infrastructure.application;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.event.PlayerLogoutEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.application.dbo.DBGame;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.entity.GameEntity;
import com.github.tix320.jouska.server.infrastructure.helper.Converters;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.util.collection.Tuple;

import static com.github.tix320.jouska.server.app.Services.GAME_ORIGIN;

public class GameManager {

	private static final SinglePublisher<None> changesPublisher = new SinglePublisher<>(None.SELF);

	private static final GameDao gameDao = new GameDao();

	public static Observable<Collection<DBGame>> games(Player caller) {
		return Observable.combineLatest(changesPublisher.asObservable(), DBGame.all().asObservable())
				.map(Tuple::second)
				.map(Map::values)
				.map(gameInfos -> {
					if (caller.isAdmin()) {
						return gameInfos;
					}
					else {
						return gameInfos.stream()
								.filter(dbGame -> hasAccessToGame(dbGame, caller))
								.collect(Collectors.toList());
					}
				});
	}

	public static String createGame(GameSettings settings, Player creator, Set<Player> accessedPlayers) {
		if (settings instanceof RestorableGameSettings) {
			DBGame dbGame = DBGame.createNew(creator, accessedPlayers, (RestorableGameSettings) settings);
			return dbGame.getId();
		}
		throw new UnsupportedOperationException();
	}

	public static GameConnectionAnswer joinGame(String gameId, Player player) {
		DBGame game = DBGame.all().get(gameId);

		if (game == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		if (!hasAccessToGame(game, player)) {
			throw new IllegalAccessException();
		}

		try {
			game.addPlayer(new GamePlayer(player,
					PlayerColor.RED)); // color will be replaced later, via method game.shufflePlayers()
			changesPublisher.publish(None.SELF);
			return GameConnectionAnswer.CONNECTED;
		}
		catch (GameIllegalStateException e) {
			return GameConnectionAnswer.ALREADY_STARTED;
		}
		catch (GameAlreadyFullException e) {
			return GameConnectionAnswer.ALREADY_FULL;
		}
	}

	public static void leaveGame(String gameId, Player player) {
		DBGame game = DBGame.all().get(gameId);

		if (game == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		if (!hasAccessToGame(game, player)) {
			throw new IllegalAccessException();
		}

		boolean removed = false;
		//noinspection SynchronizationOnLocalVariableOrMethodParameter: This is not simple local variable, it is taken from property `games`
		synchronized (game) {
			if (game.isStarted()) {
				game.kick(player);
			}
			else {
				removed = game.removePlayer(player);
			}
		}
		if (removed) {
			changesPublisher.publish(None.SELF);
		}
	}

	public static GameWatchDto watchGame(String gameId) {
		DBGame game = DBGame.all().get(gameId);
		if (game == null) {
			GameEntity gameEntity = gameDao.findById(gameId, List.of("settings", "players"))
					.orElseThrow(
							() -> new IllegalArgumentException(String.format("Game `%s` does not exists", gameId)));

			return new GameWatchDto(gameId, GameSettingsDto.fromModel(gameEntity.getSettings()),
					Converters.gamePlayerEntityToInGamePlayer(gameEntity.getPlayers()));
		}
		if (!game.isStarted()) {
			throw new IllegalStateException(String.format("Game `%s` does not started", gameId));
		}

		return new GameWatchDto(gameId, GameSettingsDto.fromModel(game.getSettings()), game.getPlayersWithColors());
	}

	public static void turnInGame(String gameId, Player player, Point point) {
		DBGame game = DBGame.all().get(gameId);
		if (game == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		//noinspection SynchronizationOnLocalVariableOrMethodParameter: This is not simple local variable, it is taken from property `games`
		synchronized (game) {
			if (!game.isStarted()) {
				throw new IllegalStateException(String.format("Game `%s` does not started", gameId));
			}

			if (!game.getPlayers().contains(player)) {
				throw new IllegalStateException(String.format("Player %s is not a player of game %s", player, gameId));
			}

			GamePlayer currentGamePlayer = game.getCurrentPlayer();

			if (!player.equals(currentGamePlayer.getRealPlayer())) {
				throw new IllegalStateException(
						String.format("Now is turn of %s, not %s", currentGamePlayer.getRealPlayer(), player));
			}

			game.turn(point);
		}
	}

	public static Optional<Game> getGame(String gameId, Player caller) {
		DBGame game = DBGame.all().get(gameId);
		if (game == null) {
			return Optional.empty();
		}

		if (!hasAccessToGame(game, caller)) {
			throw new IllegalAccessException();
		}

		return Optional.of(game);
	}

	public static void startGame(String gameId, Player caller) {
		DBGame game = DBGame.all().get(gameId);

		if (!game.getCreator().equals(caller) && !caller.isAdmin()) {
			throw new IllegalAccessException();
		}

		synchronized (game) {
			if (game.isStarted()) {
				throw new IllegalStateException(String.format("Game %s already started", gameId));
			}

			if (game.getPlayers().size() != game.getSettings().getPlayersCount()) { // full
				throw new IllegalStateException("Not fully");
			}

			TimedGameSettings gameSettings = (TimedGameSettings) game.getSettings();

			List<GamePlayer> gamePlayers = game.getPlayersWithColors();

			List<Long> clientIds = new ArrayList<>();
			for (GamePlayer player : gamePlayers) {
				clientIds.add(ClientPlayerMappingResolver.getClientIdByPlayer(player.getRealPlayer().getId())
						.orElse(null)); // null indicates offline
			}

			List<Player> offlinePlayers = new ArrayList<>();

			for (int i = 0; i < clientIds.size(); i++) {
				Long clientId = clientIds.get(i);
				if (clientId == null) {
					offlinePlayers.add(gamePlayers.get(i).getRealPlayer());
				}
			}

			if (!offlinePlayers.isEmpty()) {
				Long callerClientId = ClientPlayerMappingResolver.getClientIdByPlayer(caller.getId()).orElse(null);

				if (callerClientId == null) {
					System.err.println(String.format("Cannot start game %s(%s) and caller also %s(%s) become offline",
							gameSettings.getName(), gameId, caller.getNickname(), caller.getId()));
					return;
				}

				GAME_ORIGIN.notifyGamePlayersOffline(
						new GamePlayersOfflineWarning(gameSettings.getName(), offlinePlayers), callerClientId);

				System.err.println(String.format("Cannot start game %s(%s), because there are offline players",
						gameSettings.getName(), gameId));
				return;
			}

			List<MonoObservable<Confirmation>> playersReady = clientIds.stream()
					.map(clientId -> GAME_ORIGIN.notifyGameStartingSoon(gameSettings.getName(), clientId)
							.getOnTimout(Duration.ofSeconds(30), () -> Confirmation.REJECT))
					.collect(Collectors.toList());

			EventDispatcher.on(PlayerLogoutEvent.class).takeUntil(game.completed()).subscribe(event -> {
				Player logoutPlayer = event.getPlayer();
				if (game.getPlayers().contains(logoutPlayer)) {
					game.kick(logoutPlayer);
				}
			});

			EventDispatcher.on(PlayerDisconnectedEvent.class).takeUntil(game.completed()).subscribe(event -> {
				Player disconnectedPlayer = event.getPlayer();
				if (game.getPlayers().contains(disconnectedPlayer)) {
					game.kick(disconnectedPlayer);
				}
			});

			Observable.zip(playersReady).subscribe(confirmations -> {
				game.shufflePLayers();

				List<Player> rejectedPlayers = new ArrayList<>();
				for (int i = 0; i < confirmations.size(); i++) {
					long clientId = clientIds.get(i);
					Confirmation confirmation = confirmations.get(i);
					GamePlayer gamePlayer = gamePlayers.get(i);
					if (confirmation == Confirmation.REJECT) {
						Player player = gamePlayer.getRealPlayer();
						rejectedPlayers.add(player);
					}
					else {
						GAME_ORIGIN.notifyGameStarted(
								new GamePlayDto(gameId, GameSettingsDto.fromModel(gameSettings), gamePlayers,
										gamePlayer.getColor()), clientId);
					}
				}

				game.start();
				System.out.println(
						String.format("Game %s (%s) started on %s", gameSettings.getName(), gameId, LocalTime.now()));
				changesPublisher.publish(None.SELF);

				rejectedPlayers.forEach(game::kick);

				game.completed().subscribe(GameManager::onGameComplete);
			});
		}
	}

	private static boolean hasAccessToGame(DBGame game, Player player) {
		if (player.isAdmin() || game.getCreator().equals(player)) {
			return true;
		}

		Set<Player> accessedPlayers = game.getAccessedPlayers();

		return accessedPlayers.isEmpty() || accessedPlayers.contains(player);
	}

	private static void onGameComplete(DBGame game) {
		String gameId = game.getId();
		GameSettings gameSettings = game.getSettings();
		GamePlayer winner = game.getWinner().orElseThrow();
		System.out.println(
				String.format("Game %s(%s) ended on %s: Players %s Winner is %s", gameSettings.getName(), gameId,
						LocalTime.now(), game.getPlayersWithColors(), winner));
	}
}
