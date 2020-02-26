package com.github.tix320.jouska.core.dto;

public class GameView {

	private final long id;

	private final String name;

	private final int playersCount;

	private final int maxPlayersCount;

	private final int turnDurationSeconds;

	private final int gameDurationMinutes;

	private GameView() {
		this(-1, null, -1, -1, -1, -1);
	}

	public GameView(long id, String name, int playersCount, int maxPlayersCount, int turnDurationSeconds, int gameDurationMinutes) {
		this.id = id;
		this.name = name;
		this.playersCount = playersCount;
		this.maxPlayersCount = maxPlayersCount;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public int getMaxPlayersCount() {
		return maxPlayersCount;
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getGameDurationMinutes() {
		return gameDurationMinutes;
	}
}
