package com.github.tix320.jouska.server.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameType;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.game.SimpleJouskaGame;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.GameBoards;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.jouska.server.game.TimedJouskaGame;
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
		Player[] players = Player.getPlayers(createGameCommand.getPlayersCount());

		GameBoard board = GameBoards.defaultBoard(players);
		JouskaGame jouskaGame = TimedJouskaGame.create(SimpleJouskaGame.create(board, players),
				createGameCommand.getTurnDurationSeconds(), createGameCommand.getGameDurationMinutes());

		games.put(gameId, new GameInfo(gameId, createGameCommand.getName(), new HashSet<>(), players, new HashMap<>(),
				board.getMatrix(), jouskaGame, createGameCommand.getTurnDurationSeconds(),
				createGameCommand.getGameDurationMinutes()));
		return gameId;
	}

	public static List<GameInfo> getGames() {
		return new ArrayList<>(games.values());
	}

	public static GameConnectionAnswer connectToGame(long gameId, long clientId) {
		AtomicReference<GameConnectionAnswer> answer = new AtomicReference<>(GameConnectionAnswer.GAME_NOT_FOUND);
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			Set<Long> playersIds = gameInfo.getPlayerIds();
			Player[] players = gameInfo.getPlayers();
			if (playersIds.size() < players.length) { // free
				answer.set(GameConnectionAnswer.CONNECTED);
				gameInfo.getPlayerIds().add(clientId);
				if (playersIds.size() == players.length) { // full
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

	public static void watchGame(long gameId, long clientId) {
		// GameInfo gameInfo = games.get(gameId);
		// Lock lock = gameInfo.getLock();
		// try {
		// 	lock.lock();
		// 	WatchGameCommand watchGameCommand = new WatchGameCommand(gameId, gameInfo.getName(), gameInfo.getPlayers(),
		// 			new GameBoard(gameInfo.getGame().getBoard()));
		// 	GAME_SERVICE.watchGame(watchGameCommand, clientId)
		// 			.subscribe(none -> gameInfo.getGame()
		// 					.turns()
		// 					.subscribe(point -> IN_GAME_SERVICE.turn(point, clientId)));
		// }
		// finally {
		// 	lock.unlock();
		// }
	}

	public static void leaveFromGame(long gameId, long playerId) {
		GameInfo gameInfo = games.get(gameId);
		JouskaGame game = gameInfo.getGame();
		Lock lock = game.getLock();
		try {
			lock.lock();
			Player player = gameInfo.getPlayerById(playerId);
			game.kick(player);
		}
		finally {
			lock.unlock();
		}
	}

	public static void turnInGame(long gameId, long clientId, Point point) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		Set<Long> playerIds = gameInfo.getPlayerIds();
		if (!playerIds.contains(clientId)) {
			throw new IllegalStateException(String.format("Player %s is not a player of game %s", clientId, gameId));
		}

		Player player = gameInfo.getPlayerById(clientId);

		turnInGame(gameInfo, player, point);
	}

	private static void turnInGame(GameInfo gameInfo, Player player, Point point) {
		Lock lock = gameInfo.getGame().getLock();
		if (lock.tryLock()) {
			try {
				JouskaGame game = gameInfo.getGame();

				Player currentPlayer = game.getCurrentPlayer();

				if (player != currentPlayer) {
					throw new IllegalStateException(String.format("Now is turn of %s, not %s", currentPlayer, player));
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
		Player[] players = gameInfo.getPlayers();
		Set<Long> playerIds = gameInfo.getPlayerIds();
		List<Observable<None>> playersReady = new ArrayList<>();
		Iterator<Long> playerIdIterator = playerIds.iterator();
		TimedJouskaGame game = (TimedJouskaGame) gameInfo.getGame();
		for (Player player : players) {
			Long playerId = playerIdIterator.next();
			gameInfo.setPlayerById(playerId, player);
			Observable<None> playerReady = GAME_SERVICE.startGame(
					new StartGameCommand(gameId, gameInfo.getName(), player, players,
							new GameBoard(gameInfo.getBoard()), gameInfo.getTurnDurationSeconds(),
							gameInfo.getGameDurationMinutes(), GameType.TIMED), playerId);
			playersReady.add(playerReady);
		}

		game.turns().subscribe(rootChange -> {
			List<MonoObservable<None>> observables = playerIds.stream()
					.map(playerId -> IN_GAME_SERVICE.turn(rootChange.point, playerId))
					.collect(Collectors.toList());
			Observable.zip(observables).subscribe(nones -> {
				List<MonoObservable<None>> responses = playerIds.stream()
						.map(id -> IN_GAME_SERVICE.canTurn(id))
						.collect(Collectors.toList());
				Observable.zip(responses).subscribe(nones1 -> game.runTurnTimer());
			});
		});

		game.kickedPlayers().subscribe(playerWithPoints -> {
			Player player = playerWithPoints.player;
			Long playerId = gameInfo.getPlayerId(player);
			playerIds.remove(playerId);
			playerIds.forEach(id -> IN_GAME_SERVICE.leave(player, id));
		});

		game.onComplete().subscribe(leftPlayers -> {
			Player winner = leftPlayers.get(leftPlayers.size() - 1);
			games.remove(gameId);
			System.out.println(
					String.format("Game %s(%s) ended: Players %s Winner is %s", gameInfo.getName(), gameId, leftPlayers,
							winner));
			playerIds.forEach(id -> IN_GAME_SERVICE.forceComplete(winner, id));
		});

		Lock lock = game.getLock();

		try {
			lock.lock();

			Observable.zip(playersReady).subscribe(nones -> {
				game.start();
				System.out.println(String.format("Game %s (%s) started", gameInfo.getName(), gameId));
				List<MonoObservable<None>> responses = playerIds.stream()
						.map(id -> IN_GAME_SERVICE.canTurn(id))
						.collect(Collectors.toList());
				Observable.zip(responses).subscribe(nones1 -> game.runTurnTimer());
			});
		}
		finally {
			lock.unlock();
		}
	}
}
