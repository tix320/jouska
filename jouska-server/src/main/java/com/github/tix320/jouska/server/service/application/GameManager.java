package com.github.tix320.jouska.server.service.application;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.GameFactory;
import com.github.tix320.jouska.core.game.TimedGame;
import com.github.tix320.jouska.core.model.*;
import com.github.tix320.jouska.server.model.GameInfo;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.api.util.None;

import static com.github.tix320.jouska.server.app.Services.GAME_SERVICE;
import static com.github.tix320.jouska.server.app.Services.IN_GAME_SERVICE;

public class GameManager {
	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final Property<Map<Long, GameInfo>> gamesProperty = Property.forObject(new ConcurrentHashMap<>());

	public static long createNewGame(CreateGameCommand createGameCommand) {
		long gameId = ID_GENERATOR.next();
		GameSettings gameSettings = createGameCommand.getGameSettings();

		gamesProperty.get().put(gameId, new GameInfo(gameId, gameSettings));
		gamesProperty.reset();
		return gameId;
	}

	public static Observable<Collection<GameInfo>> games() {
		return gamesProperty.asObservable().map(Map::values);
	}

	public static GameConnectionAnswer connectToGame(long gameId, Player player) {
		AtomicReference<GameConnectionAnswer> answer = new AtomicReference<>(GameConnectionAnswer.GAME_NOT_FOUND);
		gamesProperty.get().computeIfPresent(gameId, (key, gameInfo) -> {
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
		GameInfo gameInfo = gamesProperty.get().get(gameId);
		Game game = gameInfo.getGame();
		Lock lock = gameInfo.getGameLock();
		try {
			lock.lock();
			game.kick(player);
		}
		finally {
			lock.unlock();
		}
	}

	public static void turnInGame(long gameId, Player player, Point point) {
		GameInfo gameInfo = gamesProperty.get().get(gameId);
		if (gameInfo == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		Set<Player> players = gameInfo.getConnectedPlayers();
		if (!players.contains(player)) {
			throw new IllegalStateException(String.format("Player %s is not a player of game %s", player, gameId));
		}

		turnInGame(gameInfo, player, point);
	}

	private static void turnInGame(GameInfo gameInfo, Player player, Point point) {
		Lock lock = gameInfo.getGameLock();
		try {
			lock.lock();
			Game game = gameInfo.getGame();

			InGamePlayer currentGamePlayer = game.getCurrentPlayer();

			if (!player.equals(currentGamePlayer.getPlayer())) {
				InGamePlayer gamePlayer = findGamePlayerByPlayer(game, player);
				new IllegalStateException(
						String.format("Now is turn of %s, not %s", currentGamePlayer, gamePlayer)).printStackTrace();
				return;
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

		GameBoard board = new GameBoard(game.getBoard());
		List<InGamePlayer> gamePlayers = game.getPlayers();

		game.turns().subscribe(rootChange -> onTurn(gameInfo, rootChange.point));

		game.kickedPlayers().subscribe(playerWithPoints -> onPlayerKick(gameInfo, playerWithPoints.player.getPlayer()));

		game.completed().subscribe(ignored -> onGameComplete(gameInfo));

		if (game instanceof TimedGame) {
			TimedGame timedGame = (TimedGame) game;
			timedGame.turnTimeExpiration().subscribe(none -> onTurnTimeExpiration(gameInfo));
			timedGame.gameTimeExpiration().subscribe(none -> onGameTimeExpiration(gameInfo));
		}

		Lock lock = gameInfo.getGameLock();
		try {
			lock.lock();

			List<Observable<None>> playersReady = new ArrayList<>();
			for (InGamePlayer player : gamePlayers) {
				Observable<None> playerReady = GAME_SERVICE.startGame(
						new StartGameCommand(gameId, gameSettings, player.getColor(), gamePlayers, board),
						PlayerService.getClientIdByPlayer(player.getPlayer().getId()));
				playersReady.add(playerReady);
			}

			Observable.zip(playersReady).subscribe(nones -> {
				game.start();
				System.out.println(String.format("Game %s (%s) started", gameSettings.getName(), gameId));
				List<MonoObservable<None>> responses = connectedPlayers.stream()
						.map(player -> IN_GAME_SERVICE.canTurn(PlayerService.getClientIdByPlayer(player.getId())))
						.collect(Collectors.toList());
				Observable.zip(responses).subscribe(ignored -> {
					if (game instanceof TimedGame) {
						((TimedGame) game).runTurnTimer();
					}
				});
			});
		}
		finally {
			lock.unlock();
		}
	}

	private static void onTurn(GameInfo gameInfo, Point point) {
		Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
		Set<Player> players = gameInfo.getPlayers();
		List<MonoObservable<None>> observablesForWait = new ArrayList<>();
		for (Player connectedPlayer : connectedPlayers) {
			MonoObservable<None> observable = IN_GAME_SERVICE.turn(point,
					PlayerService.getClientIdByPlayer(connectedPlayer.getId()));
			if (players.contains(connectedPlayer)) {
				observablesForWait.add(observable);
			}
		}

		Observable.zip(observablesForWait).subscribe(nones -> {
			List<MonoObservable<None>> responses = players.stream()
					.map(player -> IN_GAME_SERVICE.canTurn(PlayerService.getClientIdByPlayer(player.getId())))
					.collect(Collectors.toList());
			Observable.zip(responses).subscribe(ignored -> {
				Game game = gameInfo.getGame();
				if (game instanceof TimedGame) {
					((TimedGame) game).runTurnTimer();
				}
			});
		});
	}

	private static void onPlayerKick(GameInfo gameInfo, Player kickedPlayer) {
		Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
		Set<Player> players = gameInfo.getPlayers();
		players.remove(kickedPlayer);
		connectedPlayers.forEach(
				player -> IN_GAME_SERVICE.leave(player, PlayerService.getClientIdByPlayer(player.getId())));
	}

	private static void onGameComplete(GameInfo gameInfo) {
		long gameId = gameInfo.getId();
		Game game = gameInfo.getGame();
		GameSettings gameSettings = gameInfo.getSettings();
		Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
		InGamePlayer winner = game.winner().get();
		gamesProperty.get().remove(gameId);
		gamesProperty.reset();
		System.out.println(String.format("Game %s(%s) ended: Players %s Winner is %s", gameSettings.getName(), gameId,
				game.getPlayers(), winner));
		connectedPlayers.forEach(player -> IN_GAME_SERVICE.forceComplete(winner.getPlayer(),
				PlayerService.getClientIdByPlayer(player.getId())));
	}

	private static void onTurnTimeExpiration(GameInfo gameInfo) {
		Lock lock = gameInfo.getGameLock();
		Game game = gameInfo.getGame();
		try {
			lock.lock();
			InGamePlayer currentPlayer = game.getCurrentPlayer();
			List<Point> points = game.getPointsBelongedToPlayer(currentPlayer.getPlayer());
			int randomIndex = (int) (Math.random() * points.size());
			Point randomPoint = points.get(randomIndex);
			game.turn(randomPoint);
		}
		finally {
			lock.unlock();
		}
	}

	private static void onGameTimeExpiration(GameInfo gameInfo) {
		Lock lock = gameInfo.getGameLock();
		Game game = gameInfo.getGame();
		try {
			lock.lock();
			Map<InGamePlayer, Integer> summaryPoints = game.getStatistics().summaryPoints().get();
			Iterator<Entry<InGamePlayer, Integer>> iterator = summaryPoints.entrySet().iterator();

			Map.Entry<InGamePlayer, Integer> maxEntry = iterator.next();
			while (iterator.hasNext()) {
				Entry<InGamePlayer, Integer> entry = iterator.next();
				if (entry.getValue() > maxEntry.getValue()) {
					maxEntry = entry;
				}
			}
			InGamePlayer winner = maxEntry.getKey();
			game.forceCompleteGame(winner.getPlayer());
		}
		finally {
			lock.unlock();
		}
	}

	private static InGamePlayer findGamePlayerByPlayer(Game game, Player player) {
		return game.getPlayers()
				.stream()
				.filter(inGamePlayer -> inGamePlayer.getPlayer().equals(player))
				.findFirst()
				.orElseThrow();
	}
}
