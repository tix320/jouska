package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

public final class CreateGameCommand {

	private final TimedGameSettings gameSettings;

	private CreateGameCommand() {
		this(null);
	}

	public CreateGameCommand(TimedGameSettings gameSettings) {
		this.gameSettings = gameSettings;
	}

	public TimedGameSettings getGameSettings() {
		return gameSettings;
	}
}
