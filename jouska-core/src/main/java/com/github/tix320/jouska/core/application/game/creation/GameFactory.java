package com.github.tix320.jouska.core.application.game.creation;

import java.util.Set;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.SimpleGame;
import com.github.tix320.jouska.core.application.game.TimedGame;
import com.github.tix320.jouska.core.model.Player;

public class GameFactory {

	public static Game create(GameSettings gameSettings, Set<Player> players) {
		if (gameSettings.getPlayersCount() != players.size()) {
			throw new IllegalStateException();
		}

		if (gameSettings instanceof TimedGameSettings) {
			return createTimedGame((TimedGameSettings) gameSettings, players);
		}
		else {
			return createSimpleGame(gameSettings, players);
		}
	}

	private static Game createSimpleGame(GameSettings gameSettings, Set<Player> players) {
		return SimpleGame.createRandom(gameSettings, players);
	}

	private static Game createTimedGame(TimedGameSettings gameSettings, Set<Player> players) {
		return TimedGame.create(createSimpleGame(gameSettings, players), gameSettings);
	}
}
