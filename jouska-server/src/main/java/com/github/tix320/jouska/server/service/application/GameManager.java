package com.github.tix320.jouska.server.service.application;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.creation.GameFactory;
import com.github.tix320.jouska.core.game.creation.GameSettings;
import com.github.tix320.jouska.core.game.InGamePlayer;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.game.Point;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.event.PlayerLogoutEvent;
import com.github.tix320.jouska.server.service.ClientPlayerMappingResolver;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.MapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.api.util.None;

import static com.github.tix320.jouska.server.app.Services.GAME_SERVICE;

public class GameManager {
	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final MapProperty<Long, GameInfo> games = Property.forMap(new ConcurrentHashMap<>());

	public static long createNewGame(CreateGameCommand createGameCommand) {
		long gameId = ID_GENERATOR.next();
		GameSettings gameSettings = createGameCommand.getGameSettings();

		games.put(gameId, new GameInfo(gameId, gameSettings));
		return gameId;
	}

	public static Observable<Collection<GameInfo>> games() {
		return games.asObservable().map(Map::values);
	}

	public static GameConnectionAnswer connectToGame(long gameId, Player player) {
		AtomicReference<GameConnectionAnswer> answer = new AtomicReference<>(GameConnectionAnswer.GAME_NOT_FOUND);
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			int playersCount = gameInfo.getSettings().getPlayersCount();
			Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
			if (connectedPlayers.size() < playersCount) { // free
				answer.set(GameConnectionAnswer.CONNECTED);
				connectedPlayers.add(player);
				gameInfo.getPlayers().add(player);
				if (connectedPlayers.size() == playersCount) { // full
					startGame(gameInfo);
				}
			}
			else {
				answer.set(GameConnectionAnswer.ALREADY_STARTED);
			}

			return gameInfo;
		});
		return answer.get();
	}

	public static void watchGame(long gameId, long playerId) {
		// GameInfo gameInfo = games.get(gameId);
		// Lock lock = gameInfo.getLock();
		// try {
		// 	lock.lock();
		// 	WatchGameCommand watchGameCommand = new WatchGameCommand(gameId, gameInfo.getName(), gameInfo.getPlayers(),
		// 			new GameBoard(gameInfo.getGame().getBoard()));
		// 	GAME_SERVICE.watchGame(watchGameCommand, playerId)
		// 			.subscribe(none -> gameInfo.getGame()
		// 					.turns()
		// 					.subscribe(point -> IN_GAME_SERVICE.turn(point, playerId)));
		// }
		// finally {
		// 	lock.unlock();
		// }
	}

	public static void leaveFromGame(long gameId, Player player) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null) {
			throw new IllegalArgumentException(String.format("Game `%s` does not exists", gameId));
		}

		removePLayerFromGame(gameInfo, player);
	}

	public static void turnInGame(long gameId, Player player, Point point) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		Set<Player> players = gameInfo.getConnectedPlayers();
		if (!players.contains(player)) {
			throw new IllegalStateException(String.format("Player %s is not a player of game %s", player, gameId));
		}

		turnInGame(gameInfo, player, point);
	}

	public static Game getGame(long gameId) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null) {
			throw new IllegalArgumentException(String.format("Game %s not found", gameId));
		}

		return gameInfo.getGame();
	}

	private static void turnInGame(GameInfo gameInfo, Player player, Point point) {
		Lock lock = gameInfo.getGameLock();
		lock.lock();
		try {
			Game game = gameInfo.getGame();

			InGamePlayer currentGamePlayer = game.getCurrentPlayer();

			if (!player.equals(currentGamePlayer.getRealPlayer())) {
				throw new IllegalStateException(
						String.format("Now is turn of %s, not %s", currentGamePlayer.getRealPlayer(), player));
			}

			game.turn(point);
		}
		finally {
			lock.unlock();
		}
	}

	private static void startGame(GameInfo gameInfo) {
		long gameId = gameInfo.getId();
		GameSettings gameSettings = gameInfo.getSettings();
		Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();

		Game game = GameFactory.create(gameSettings, connectedPlayers);
		gameInfo.setGame(game);

		List<InGamePlayer> gamePlayers = game.getPlayers();

		game.completed().subscribe(ignored -> onGameComplete(gameInfo));

		Lock lock = gameInfo.getGameLock();
		try {
			lock.lock();

			List<Observable<None>> playersReady = new ArrayList<>();
			for (InGamePlayer player : gamePlayers) {
				ClientPlayerMappingResolver.getClientIdByPlayer(player.getRealPlayer().getId())
						.ifPresentOrElse(clientId -> {
							Observable<None> playerReady = GAME_SERVICE.startGame(
									new StartGameCommand(gameId, gameSettings, player.getColor(), gamePlayers),
									clientId);
							playersReady.add(playerReady);
						}, () -> logPlayerConnectionNotFound(player.getRealPlayer()));
			}

			EventDispatcher.on(PlayerLogoutEvent.class).takeUntil(game.completed()).subscribe(event -> {
				Player logoutPlayer = event.getPlayer();
				if (containsPlayerInGame(game, logoutPlayer)) {
					removePLayerFromGame(gameInfo, logoutPlayer);
				}
			});

			EventDispatcher.on(PlayerDisconnectedEvent.class).takeUntil(game.completed()).subscribe(event -> {
				Player disconnectedPlayer = event.getPlayer();
				if (containsPlayerInGame(game, disconnectedPlayer)) {
					removePLayerFromGame(gameInfo, disconnectedPlayer);
				}
			});

			Observable.zip(playersReady).subscribe(nones -> {
				game.start();
				System.out.println(String.format("Game %s (%s) started", gameSettings.getName(), gameId));
			});
		}
		finally {
			lock.unlock();
		}
	}

	private static void removePLayerFromGame(GameInfo gameInfo, Player player) {
		Lock gameLock = gameInfo.getGameLock();
		gameLock.lock();
		try {
			Game game = gameInfo.getGame();
			gameInfo.getConnectedPlayers().remove(player);
			game.kick(player);
			Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
			Set<Player> players = gameInfo.getPlayers();
			players.remove(player);
			connectedPlayers.remove(player);
		}
		finally {
			gameLock.unlock();
		}
	}

	private static void onGameComplete(GameInfo gameInfo) {
		long gameId = gameInfo.getId();
		Game game = gameInfo.getGame();
		GameSettings gameSettings = gameInfo.getSettings();
		InGamePlayer winner = game.getWinner().orElseThrow();
		games.remove(gameId);
		System.out.println(String.format("Game %s(%s) ended: Players %s Winner is %s", gameSettings.getName(), gameId,
				game.getPlayers(), winner));
	}

	private static boolean containsPlayerInGame(Game game, Player player) {
		for (InGamePlayer gamePlayer : game.getPlayers()) {
			if (player.equals(gamePlayer.getRealPlayer())) {
				return true;
			}
		}
		return false;
	}

	private static void logPlayerConnectionNotFound(Player player) {
		new IllegalStateException(String.format("No connection found for player %s", player)).printStackTrace();
	}
}
