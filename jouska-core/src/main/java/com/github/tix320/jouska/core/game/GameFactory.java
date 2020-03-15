package com.github.tix320.jouska.core.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.model.*;

public class GameFactory {

	public static Game create(GameSettings gameSettings, Set<Player> players) {
		if (gameSettings.getPlayersCount() != players.size()) {
			throw new IllegalStateException();
		}

		switch (gameSettings.getGameType()) {
			case SIMPLE:
				return createSimpleGame(gameSettings, players);
			case TIMED:
				return createTimedGame(gameSettings, players);
			default:
				throw new IllegalStateException();
		}
	}

	private static Game createSimpleGame(GameSettings gameSettings, Set<Player> players) {
		PlayerColor[] playerColors = PlayerColor.getRandomPlayers(gameSettings.getPlayersCount());

		List<Player> playersList = players.stream().collect(Collectors.collectingAndThen(Collectors.toList(), ids -> {
			Collections.shuffle(ids);
			return ids;
		}));

		List<InGamePlayer> gamePlayers = new ArrayList<>(playersList.size());
		for (int i = 0; i < playersList.size(); i++) {
			Player player = playersList.get(i);
			PlayerColor playerColor = playerColors[i];
			gamePlayers.add(new InGamePlayer(player, playerColor));
		}

		GameBoard board = GameBoards.createByType(gameSettings.getBoardType(), playerColors);
		return SimpleGame.create(gameSettings, board, gamePlayers);
	}

	private static Game createTimedGame(GameSettings gameSettings, Set<Player> players) {
		return TimedGame.create(createSimpleGame(gameSettings, players), gameSettings.getTurnDurationSeconds(),
				gameSettings.getGameDurationMinutes());
	}
}
