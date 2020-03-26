package com.github.tix320.jouska.core.game.creation;

import java.util.Set;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.SimpleGame;
import com.github.tix320.jouska.core.game.TimedGame;
import com.github.tix320.jouska.core.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.Player;

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
		return SimpleGame.createRandom(gameSettings, players);
	}

	private static Game createTimedGame(GameSettings gameSettings, Set<Player> players) {
		return TimedGame.create(createSimpleGame(gameSettings, players), gameSettings.getTurnDurationSeconds(),
				gameSettings.getGameDurationMinutes());
	}
}
