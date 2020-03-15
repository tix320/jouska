package com.github.tix320.jouska.server.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.api.util.None;

import static com.github.tix320.jouska.server.app.Services.GAME_SERVICE;
import static com.github.tix320.jouska.server.app.Services.IN_GAME_SERVICE;

public class GameManager {
	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final Map<Long, GameInfo> games = new ConcurrentHashMap<>();

	public static long createNewGame(CreateGameCommand createGameCommand) {
		long gameId = ID_GENERATOR.next();
		GameSettings gameSettings = createGameCommand.getGameSettings();

		games.put(gameId, new GameInfo(gameId, gameSettings));
		return gameId;
	}

	public static List<GameInfo> getGames() {
		return new ArrayList<>(games.values());
	}

	public static GameConnectionAnswer connectToGame(long gameId, Player player) {
		AtomicReference<GameConnectionAnswer> answer = new AtomicReference<>(GameConnectionAnswer.GAME_NOT_FOUND);
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			int playersCount = gameInfo.getSettings().getPlayersCount();
			Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
			if (connectedPlayers.size() < playersCount) { // free
				answer.set(GameConnectionAnswer.CONNECTED);
				connectedPlayers.add(player);
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
		Game game = gameInfo.getGame();
		Lock lock = game.getLock();
		try {
			lock.lock();
			game.kick(player);
		}
		finally {
			lock.unlock();
		}
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

	private static void turnInGame(GameInfo gameInfo, Player player, Point point) {
		Lock lock = gameInfo.getGame().getLock();
		if (lock.tryLock()) {
			try {
				Game game = gameInfo.getGame();

				InGamePlayer currentGamePlayer = game.getCurrentPlayer();

				if (!player.equals(currentGamePlayer.getPlayer())) {
					InGamePlayer gamePlayer = findGamePlayerByPlayer(game, player);
					throw new IllegalStateException(
							String.format("Now is turn of %s, not %s", currentGamePlayer, gamePlayer));
				}

				game.turn(point);
			}
			finally {
				lock.unlock();
			}
		}
	}

	private static void startGame(GameInfo gameInfo) {
		long gameId = gameInfo.getId();
		GameSettings gameSettings = gameInfo.getSettings();
		Set<Player> players = gameInfo.getConnectedPlayers();

		TimedGame game = (TimedGame) GameFactory.create(gameSettings, players);
		gameInfo.setGame(game);

		GameBoard board = new GameBoard(game.getBoard());
		List<InGamePlayer> gamePlayers = game.getPlayers();

		List<Observable<None>> playersReady = new ArrayList<>();
		for (InGamePlayer player : gamePlayers) {
			Observable<None> playerReady = GAME_SERVICE.startGame(
					new StartGameCommand(gameId, gameSettings, player.getColor(), gamePlayers, board),
					PlayerService.getClientIdByPlayer(player.getPlayer().getId()));
			playersReady.add(playerReady);
		}

		game.turns().asObservable().subscribe(rootChange -> {
			List<MonoObservable<None>> observables = players.stream()
					.map(player -> IN_GAME_SERVICE.turn(rootChange.point,
							PlayerService.getClientIdByPlayer(player.getId())))
					.collect(Collectors.toList());
			Observable.zip(observables).subscribe(nones -> {
				List<MonoObservable<None>> responses = players.stream()
						.map(player -> IN_GAME_SERVICE.canTurn(PlayerService.getClientIdByPlayer(player.getId())))
						.collect(Collectors.toList());
				Observable.zip(responses).subscribe(nones1 -> game.runTurnTimer());
			});
		});

		game.kickedPlayers().asObservable().subscribe(playerWithPoints -> {
			InGamePlayer gamePlayer = playerWithPoints.player;
			players.remove(gamePlayer.getPlayer());
			players.forEach(
					player -> IN_GAME_SERVICE.leave(gamePlayer, PlayerService.getClientIdByPlayer(player.getId())));
		});

		game.onComplete().subscribe(leftPlayers -> {
			InGamePlayer winner = leftPlayers.get(leftPlayers.size() - 1);
			games.remove(gameId);
			System.out.println(
					String.format("Game %s(%s) ended: Players %s Winner is %s", gameSettings.getName(), gameId,
							leftPlayers, winner));
			players.forEach(player -> IN_GAME_SERVICE.forceComplete(winner.getPlayer(),
					PlayerService.getClientIdByPlayer(player.getId())));
		});

		Lock lock = game.getLock();

		try {
			lock.lock();

			Observable.zip(playersReady).subscribe(nones -> {
				game.start();
				System.out.println(String.format("Game %s (%s) started", gameSettings.getName(), gameId));
				List<MonoObservable<None>> responses = players.stream()
						.map(player -> IN_GAME_SERVICE.canTurn(PlayerService.getClientIdByPlayer(player.getId())))
						.collect(Collectors.toList());
				Observable.zip(responses).subscribe(nones1 -> game.runTurnTimer());
			});
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
