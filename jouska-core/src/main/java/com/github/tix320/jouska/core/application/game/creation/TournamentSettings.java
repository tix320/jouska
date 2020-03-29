package com.github.tix320.jouska.core.application.game.creation;

public final class TournamentSettings {

	private final String name;

	private final int playersCount;

	private final TimedGameSettings groupGameSettings;

	private final TimedGameSettings playOffGameSettings;

	private TournamentSettings() {
		this(null, -1, null, null);
	}

	public TournamentSettings(String name, int playersCount, TimedGameSettings groupGameSettings,
							  TimedGameSettings playOffGameSettings) {
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

	public TimedGameSettings getGroupGameSettings() {
		return groupGameSettings;
	}

	public TimedGameSettings getPlayOffGameSettings() {
		return playOffGameSettings;
	}
}
