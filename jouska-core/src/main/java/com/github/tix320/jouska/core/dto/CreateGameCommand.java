package com.github.tix320.jouska.core.dto;

public final class CreateGameCommand {

	private final String name;

	private final int playersCount;

	private final int turnDurationSeconds;

	private final int gameDurationMinutes;

	private CreateGameCommand() {
		this("auu", 1, -1, -1);
	}

	public CreateGameCommand(String name, int playersCount, int turnDurationSeconds, int gameDurationMinutes) {
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Empty name");
		}
		if (!(playersCount > 0 && playersCount <= 4)) {
			throw new IllegalArgumentException(String.format("Invalid players count %s", playersCount));
		}

		this.name = name;
		this.playersCount = playersCount;
	}

	public String getName() {
		return name;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getGameDurationMinutes() {
		return gameDurationMinutes;
	}
}
