package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.BoardType;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class TimedGameSettings extends GameSettings {

	private final int turnDurationSeconds;

	private final int playerTurnTotalDurationSeconds;

	private final int gameDurationMinutes;

	private TimedGameSettings() {
		turnDurationSeconds = -1;
		playerTurnTotalDurationSeconds = -1;
		gameDurationMinutes = -1;
	}

	public TimedGameSettings(String name, BoardType boardType, int playersCount, int turnDurationSeconds,
							 int playerTurnTotalDurationSeconds, int gameDurationMinutes) {
		super(name, boardType, playersCount);
		this.turnDurationSeconds = turnDurationSeconds;
		this.playerTurnTotalDurationSeconds = playerTurnTotalDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;

		if (turnDurationSeconds < 1) {
			throw new IllegalArgumentException(String.format("Invalid turn duration %s", turnDurationSeconds));
		}

		if (gameDurationMinutes < 1) {
			throw new IllegalArgumentException(String.format("Invalid game duration %s", gameDurationMinutes));
		}
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getPlayerTurnTotalDurationSeconds() {
		return playerTurnTotalDurationSeconds;
	}

	public int getGameDurationMinutes() {
		return gameDurationMinutes;
	}
}
