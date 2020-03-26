package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.game.creation.GameSettings;

public final class CreateGameCommand {

	private final GameSettings gameSettings;

	private CreateGameCommand() {
		this(null);
	}

	public CreateGameCommand(GameSettings gameSettings) {
		this.gameSettings = gameSettings;
	}

	public GameSettings getGameSettings() {
		return gameSettings;
	}
}
