package com.github.tix320.jouska.server.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.jouska.core.model.GameBoards;
import com.github.tix320.jouska.core.model.GameInfo;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Turn;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;
import com.github.tix320.sonder.api.common.topic.Topic;

import static com.github.tix320.jouska.server.app.Services.GAME_SERVICE;
import static com.github.tix320.jouska.server.app.Services.SONDER;

@Endpoint("game")
public class GameEndpoint {

	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final Map<Long, GameInfo> games = new ConcurrentHashMap<>();
	private static final Map<Long, Lock> gameLocks = new ConcurrentHashMap<>();

	@Endpoint("info")
	public List<GameInfo> getGames() {
		return new ArrayList<>(games.values());
	}

	@Endpoint("connect")
	public GameConnectionAnswer connect(long gameId, @ClientID long clientId) {
		AtomicReference<GameConnectionAnswer> answer = new AtomicReference<>(GameConnectionAnswer.GAME_NOT_FOUND);
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			Set<Long> players = gameInfo.getPlayers();
			if (players.size() < gameInfo.getMaxPlayers()) { // free
				answer.set(GameConnectionAnswer.CONNECTED);
				players.add(clientId);
				if (players.size() == gameInfo.getMaxPlayers()) { // full
					List<Observable<None>> playersReady = new ArrayList<>();
					int playerNumber = 1;
					Player firstPlayer = gameInfo.getFirstTurnPlayer();
					Topic<Turn> topic = SONDER.registerTopic("game: " + gameId, new TypeReference<>() {});
					AtomicInteger turnCount = new AtomicInteger();
					topic.asObservable().subscribe(turn -> {
						Lock lock = gameLocks.get(gameId);
						try {
							lock.lock();
							gameInfo.addTurn(turn.changeNumber(turnCount.incrementAndGet()));
						}
						finally {
							lock.unlock();
						}
					});
					for (Long playerId : players) {
						Observable<None> playerReady = GAME_SERVICE.startGame(
								new StartGameCommand(gameId, gameInfo.getName(), Player.fromNumber(playerNumber++),
										firstPlayer, players.size(), gameInfo.getInitialGameBoard()), playerId);
						playersReady.add(playerReady);
					}
					Observable.combine(playersReady).subscribe(nones -> System.out.println("Game started"));
				}
			}
			else {
				answer.set(GameConnectionAnswer.ALREADY_STARTED);
			}

			return gameInfo;
		});
		return answer.get();
	}

	@Endpoint("create")
	public long createGame(CreateGameCommand createGameCommand, @ClientID long clientId) {
		long gameId = ID_GENERATOR.next();
		games.put(gameId, new GameInfo(gameId, createGameCommand.getName(), new HashSet<>(),
				Player.fromNumber(new Random().nextInt(createGameCommand.getPlayersCount()) + 1),
				createGameCommand.getPlayersCount(), GameBoards.defaultBoard(createGameCommand.getPlayersCount()),
				new ArrayList<>()));
		gameLocks.put(gameId, new ReentrantLock());
		return gameId;
	}

	@Endpoint("watch")
	public void watchGame(long gameId, @ClientID long clientId) {
		Lock lock = gameLocks.get(gameId);
		try {
			lock.lock();
			GameInfo gameInfo = games.get(gameId);
			WatchGameCommand watchGameCommand = new WatchGameCommand(gameId, gameInfo.getName(),
					gameInfo.getFirstTurnPlayer(), gameInfo.getMaxPlayers(), gameInfo.getInitialGameBoard(),
					gameInfo.getTurns());
			GAME_SERVICE.watchGame(watchGameCommand, clientId);
		}
		finally {
			lock.unlock();
		}
	}
}
