package com.gitlab.tixtix320.jouska.server.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.GameBoards;
import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import com.gitlab.tixtix320.kiwi.api.util.IDGenerator;
import com.gitlab.tixtix320.sonder.api.common.rpc.Endpoint;
import com.gitlab.tixtix320.sonder.api.common.rpc.extra.ClientID;
import com.gitlab.tixtix320.sonder.api.common.topic.TopicPublisher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.gitlab.tixtix320.jouska.server.app.Services.SONDER;

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
    public String connect(long gameId, @ClientID long clientId) {
        AtomicReference<String> status = new AtomicReference<>("invalid-game-id");
        games.computeIfPresent(gameId, (key, gameInfo) -> {
            int players = gameInfo.getPlayers();
            if (players < gameInfo.getMaxPlayers()) { // free
                status.set("connected");
                gamePlayers.get(gameId).add(clientId);
                GameInfo newGameInfo = new GameInfo(key, gameInfo.getName(), players + 1, gameInfo.getMaxPlayers());
                if (newGameInfo.getPlayers() == newGameInfo.getMaxPlayers()) { // full
                    TopicPublisher<GameBoard> publisher = SONDER.registerTopicPublisher("game-board: " + gameId, new TypeReference<>() {
                    });
                    GameBoard board = GameBoards.defaultBoard(newGameInfo.getPlayers());
                    publisher.publish(board);
                }
                gamePlayers.get(key).add(clientId);
                return newGameInfo;
            } else {
                status.set("full");
                return gameInfo;
            }
        });
        return status.get();
    }

    @Endpoint("create")
    public long createGame(GameInfo gameInfo, @ClientID long clientId) {
        long gameId = ID_GENERATOR.next();
        games.put(clientId, new GameInfo(gameId, gameInfo.getName(), 0, gameInfo.getMaxPlayers()));
        Set<Long> clientIds = new HashSet<>();
        clientIds.add(clientId);
        gamePlayers.put(gameId, clientIds);
        return gameId;
    }
}
