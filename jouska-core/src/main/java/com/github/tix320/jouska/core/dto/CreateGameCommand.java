package com.github.tix320.jouska.core.dto;

import java.util.Set;

import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

public final class CreateGameCommand {

	private final TimedGameSettings gameSettings;

	private final Set<String> accessedPlayers;

	private CreateGameCommand() {
		this(null, null);
	}

	public CreateGameCommand(TimedGameSettings gameSettings, Set<String> accessedPlayers) {
		this.gameSettings = gameSettings;
		this.accessedPlayers = accessedPlayers;
	}

	public Set<String> getAccessedPlayers() {
		return accessedPlayers;
	}

	public TimedGameSettings getGameSettings() {
		return gameSettings;
	}
}
