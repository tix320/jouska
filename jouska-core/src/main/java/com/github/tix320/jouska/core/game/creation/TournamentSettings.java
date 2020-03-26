package com.github.tix320.jouska.core.game.creation;

public final class TournamentSettings {

	private final String name;

	private final int playersCount;

	private final GameSettings groupGameSettings;

	private final GameSettings playOffGameSettings;

	public TournamentSettings(String name, int playersCount, GameSettings groupGameSettings, GameSettings playOffGameSettings) {
		this.name = name;
		this.playersCount = playersCount;
		this.groupGameSettings = groupGameSettings;
		this.playOffGameSettings = playOffGameSettings;
	}

	public String getName() {
		return name;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public GameSettings getGroupGameSettings() {
		return groupGameSettings;
	}

	public GameSettings getPlayOffGameSettings() {
		return playOffGameSettings;
	}
}
