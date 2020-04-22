package com.github.tix320.jouska.core.dto;

import java.util.Set;

import com.github.tix320.jouska.core.model.Player;

public final class CreateGameCommand {

	private final GameSettingsDto gameSettings;

	private final Set<Player> accessedPlayers;

	private CreateGameCommand() {
		this(null, null);
	}

	public CreateGameCommand(GameSettingsDto gameSettings, Set<Player> accessedPlayers) {
		this.gameSettings = gameSettings;
		this.accessedPlayers = accessedPlayers;
	}

	public GameSettingsDto getGameSettings() {
		return gameSettings;
	}

	public Set<Player> getAccessedPlayers() {
		return accessedPlayers;
	}
}
