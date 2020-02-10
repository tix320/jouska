package com.github.tix320.jouska.server.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.GameBoards;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.api.util.None;

import static com.github.tix320.jouska.server.app.Services.GAME_SERVICE;
import static com.github.tix320.jouska.server.app.Services.IN_GAME_SERVICE;

public class GameManager {
	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final Map<Long, GameInfo> games = new ConcurrentHashMap<>();
	private static final Map<Long, Lock> gameLocks = new ConcurrentHashMap<>();

	public static long createNewGame(CreateGameCommand createGameCommand) {
		long gameId = ID_GENERATOR.next();
		Player[] players = Player.getPlayers(createGameCommand.getPlayersCount());

		games.put(gameId, new GameInfo(gameId, createGameCommand.getName(), new HashSet<>(), players, new HashMap<>(),
				new JouskaGame(GameBoards.defaultBoard(players), players)));
		gameLocks.put(gameId, new ReentrantLock());
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
				gameInfo.addPlayer(clientId, playersIds.size());
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
		Lock lock = gameLocks.get(gameId);
		try {
			lock.lock();
			GameInfo gameInfo = games.get(gameId);
			WatchGameCommand watchGameCommand = new WatchGameCommand(gameId, gameInfo.getName(), gameInfo.getPlayers(),
					new GameBoard(gameInfo.getGame().getBoard()), gameInfo.getGame().getTurnList());
			GAME_SERVICE.watchGame(watchGameCommand, clientId);
		}
		finally {
			lock.unlock();
		}
	}

	public static void turnInGame(long gameId, long clientId, Point point) {
		GameInfo gameInfo = games.get(gameId);
		Lock lock = gameLocks.get(gameId);
		try {
			lock.lock();
			JouskaGame game = gameInfo.getGame();
			Set<Long> playerIds = gameInfo.getPlayerIds();
			if (!playerIds.contains(clientId)) {
				throw new IllegalStateException(
						String.format("Player %s is not a player of game %s", clientId, gameId));
			}

			Player currentPlayer = game.getCurrentPlayer();
			Player player = gameInfo.getPlayer(clientId);

			if (player != currentPlayer) {
				throw new IllegalStateException(String.format("Now is turn of %s, not %s", currentPlayer, player));
			}

			game.turn(point);
			for (Long playerId : playerIds) {
			IN_GAME_SERVICE.turn(point,playerId);

			}
			// Observable.combine(
			// 		playerIds.stream().map(id -> IN_GAME_SERVICE.turn(point, id)).collect(Collectors.toList()))
			// 		.waitComplete().subscribe(nones -> {});

			List<Player> losePlayers = game.getLosePlayers();
			for (Player losePlayer : losePlayers) {
						playerIds.stream().map(id -> IN_GAME_SERVICE.lose(losePlayer, id)).collect(Collectors.toList());
			}

			game.getWinPlayer().ifPresent(winPlayer -> {
				playerIds.stream().map(id -> IN_GAME_SERVICE.win(winPlayer, id)).collect(Collectors.toList());
				games.remove(gameId);
				gameLocks.remove(gameId);
			});
		}
		finally {
			lock.unlock();
		}
	}

	private static void startGame(GameInfo gameInfo) {
		long gameId = gameInfo.getId();
		Player[] players = gameInfo.getPlayers();
		Set<Long> playerIds = gameInfo.getPlayerIds();
		List<Observable<None>> playersReady = new ArrayList<>();
		Iterator<Long> playerIdIterator = playerIds.iterator();
		for (Player player : players) {
			Long playerId = playerIdIterator.next();
			Observable<None> playerReady = GAME_SERVICE.startGame(
					new StartGameCommand(gameId, gameInfo.getName(), player, players,
							new GameBoard(gameInfo.getGame().getBoard())), playerId);
			playersReady.add(playerReady);
		}
		Observable.combine(playersReady)
				.subscribe(
						nones -> System.out.println(String.format("Game %s (%s) started", gameInfo.getName(), gameId)));
	}

}
