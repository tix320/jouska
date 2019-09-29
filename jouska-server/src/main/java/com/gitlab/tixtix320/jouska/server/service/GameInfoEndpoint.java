package com.gitlab.tixtix320.jouska.server.service;

import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import com.gitlab.tixtix320.jouska.server.app.Services;
import com.gitlab.tixtix320.jouska.server.game.GameBoards;
import com.gitlab.tixtix320.kiwi.api.util.IDGenerator;
import com.gitlab.tixtix320.sonder.api.common.Endpoint;
import com.gitlab.tixtix320.sonder.api.common.extra.ClientID;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Endpoint("game")
public class GameInfoEndpoint {

    private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
    private static final Map<Long, GameInfo> games = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> gamePlayers = new ConcurrentHashMap<>();

    @Endpoint("info")
    public List<GameInfo> getGames() {
        return new ArrayList<>(games.values());
    }

    @Endpoint("connect")
    public Boolean connect(long gameId, @ClientID long clientId) {
        AtomicBoolean joined = new AtomicBoolean();
        games.computeIfPresent(gameId, (key, gameInfo) -> {
            int players = gameInfo.getPlayers();
            if (players < gameInfo.getMaxPlayers()) {
                GameInfo newGameInfo = new GameInfo(key, gameInfo.getName(), players + 1, gameInfo.getMaxPlayers());
                if (newGameInfo.getPlayers() == newGameInfo.getMaxPlayers()) { // fulled
                    GameBoard board = GameBoards.defaultBoard(newGameInfo.getPlayers());
                    gamePlayers.get(key).forEach(value -> Services.GAME_SERVICE.sendBoard(board, value));
                }
                joined.set(true);
                gamePlayers.get(key).add(clientId);
                return newGameInfo;
            } else {
                return gameInfo;
            }
        });
        return joined.get();
    }

    @Endpoint("create")
    public void createGame(GameInfo gameInfo, @ClientID long clientId) {
        long gameId = ID_GENERATOR.next();
        games.put(clientId, new GameInfo(gameId, gameInfo.getName(), 1, gameInfo.getMaxPlayers()));
        Set<Long> clientIds = new HashSet<>();
        clientIds.add(clientId);
        gamePlayers.put(gameId, clientIds);
    }
}
