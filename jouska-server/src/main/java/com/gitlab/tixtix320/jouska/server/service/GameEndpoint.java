package com.gitlab.tixtix320.jouska.server.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.GameBoards;
import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import com.gitlab.tixtix320.jouska.core.model.Player;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.util.IDGenerator;
import com.gitlab.tixtix320.kiwi.api.util.None;
import com.gitlab.tixtix320.sonder.api.common.rpc.Endpoint;
import com.gitlab.tixtix320.sonder.api.common.rpc.extra.ClientID;

import static com.gitlab.tixtix320.jouska.server.app.Services.GAME_SERVICE;

@Endpoint("game")
public class GameEndpoint {

	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final Map<Long, GameInfo> games = new ConcurrentHashMap<>();
	private static final Map<Long, Set<Long>> gamePlayers = new ConcurrentHashMap<>();

	@Endpoint("info")
	public List<GameInfo> getGames() {
		return new ArrayList<>(games.values());
	}

	@Endpoint("connect")
	public void connect(long gameId, @ClientID long clientId) {
		AtomicReference<String> status = new AtomicReference<>("invalid-game-id");
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			int players = gameInfo.getPlayers();
			if (players < gameInfo.getMaxPlayers()) { // free
				status.set("connected");
				Set<Long> gamePlayerIds = gamePlayers.get(gameId);
				gamePlayerIds.add(clientId);
				GameInfo newGameInfo = new GameInfo(key, gameInfo.getName(), players + 1, gameInfo.getMaxPlayers());
				if (newGameInfo.getPlayers() == newGameInfo.getMaxPlayers()) { // full
					List<Observable<None>> playersReady = new ArrayList<>();
					int playerNumber = 1;
					for (Long playerId : gamePlayerIds) {
						GameBoard board = GameBoards.defaultBoard(newGameInfo.getPlayers());
						Observable<None> playerReady = GAME_SERVICE.startGame(gameId, board,
								Player.fromNumber(playerNumber++), Player.fromNumber(1), gamePlayerIds.size(),
								playerId);
						playersReady.add(playerReady);
					}
					Observable.combine(playersReady).subscribe(nones -> {
						System.out.println("Game started");
					});
				}
				gamePlayers.get(key).add(clientId);
				return newGameInfo;
			}
			else {
				status.set("full");
				return gameInfo;
			}
		});
		//        return status.get();
	}

	@Endpoint("create")
	public long createGame(GameInfo gameInfo, @ClientID long clientId) {
		long gameId = ID_GENERATOR.next();
		games.put(gameId, new GameInfo(gameId, gameInfo.getName(), 0, gameInfo.getMaxPlayers()));
		Set<Long> clientIds = new HashSet<>();
		clientIds.add(clientId);
		gamePlayers.put(gameId, clientIds);
		return gameId;
	}
}
