package com.github.tix320.jouska.core.game.creation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.game.BoardType;

public class GameSettings {

	private final String name;

	private final GameType gameType;

	private final BoardType boardType;

	private final int playersCount;

	private final int turnDurationSeconds;

	private final int gameDurationMinutes;

	@JsonCreator
	public GameSettings(@JsonProperty("name") String name, @JsonProperty("gameType") GameType gameType,
						@JsonProperty("boardType") BoardType boardType, @JsonProperty("playersCount") int playersCount,
						@JsonProperty("turnDurationSeconds") int turnDurationSeconds,
						@JsonProperty("gameDurationMinutes") int gameDurationMinutes) {
		this.name = name;
		this.gameType = gameType;
		this.boardType = boardType;
		this.playersCount = playersCount;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Empty name");
		}
		if (!(playersCount > 0 && playersCount <= 4)) {
			throw new IllegalArgumentException(String.format("Invalid players count %s", playersCount));
		}

		if (turnDurationSeconds < 1) {
			throw new IllegalArgumentException(String.format("Invalid turn duration %s", turnDurationSeconds));
		}

		if (gameDurationMinutes < 1) {
			throw new IllegalArgumentException(String.format("Invalid game duration %s", gameDurationMinutes));
		}
	}

	public GameSettings(GameType gameType, BoardType boardType, int turnDurationSeconds, int gameDurationMinutes) {
		this.name = "None";
		this.playersCount = 2;
		this.gameType = gameType;
		this.boardType = boardType;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;

		if (turnDurationSeconds < 1) {
			throw new IllegalArgumentException(String.format("Invalid turn duration %s", turnDurationSeconds));
		}

		if (gameDurationMinutes < 1) {
			throw new IllegalArgumentException(String.format("Invalid game duration %s", gameDurationMinutes));
		}
	}

	public String getName() {
		return name;
	}

	public GameType getGameType() {
		return gameType;
	}

	public BoardType getBoardType() {
		return boardType;
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
