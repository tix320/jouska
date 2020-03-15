package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.model.GameSettings;

public class CreateTournamentCommand {

	private final String name;

	private final int playersCount;

	private final GameSettings groupSettings;

	private final GameSettings playOffSettings;

	private CreateTournamentCommand() {
		this(null, -1, null, null);
	}

	public CreateTournamentCommand(String name, int playersCount, GameSettings groupSettings,
								   GameSettings playOffSettings) {
		this.name = name;
		this.playersCount = playersCount;
		this.groupSettings = groupSettings;
		this.playOffSettings = playOffSettings;
	}

	public String getName() {
		return name;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public GameSettings getGroupSettings() {
		return groupSettings;
	}

	public GameSettings getPlayOffSettings() {
		return playOffSettings;
	}
}
