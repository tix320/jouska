package com.github.tix320.jouska.core.application.game;

import com.github.tix320.jouska.core.application.game.creation.GameSettings;

/**
 * @author tigra on 06-Apr-20.
 */
public class GameWithSettings {

	private final Game game;

	private final GameSettings settings;

	public GameWithSettings(Game game, GameSettings settings) {
		this.game = game;
		this.settings = settings;
	}

	public Game getGame() {
		return game;
	}

	public GameSettings getSettings() {
		return settings;
	}
}
