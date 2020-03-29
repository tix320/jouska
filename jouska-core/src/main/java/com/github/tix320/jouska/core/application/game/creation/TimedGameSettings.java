package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.BoardType;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class TimedGameSettings extends GameSettings {

	private final int turnDurationSeconds;

	private final int playerTurnTotalDurationSeconds;

	private TimedGameSettings() {
		turnDurationSeconds = -1;
		playerTurnTotalDurationSeconds = -1;
	}

	public TimedGameSettings(String name, BoardType boardType, int playersCount, int turnDurationSeconds,
							 int playerTurnTotalDurationSeconds) {
		super(name, boardType, playersCount);
		this.turnDurationSeconds = turnDurationSeconds;
		this.playerTurnTotalDurationSeconds = playerTurnTotalDurationSeconds;

		if (turnDurationSeconds < 1) {
			throw new IllegalArgumentException(String.format("Invalid turn duration %s", turnDurationSeconds));
		}

		if (playerTurnTotalDurationSeconds < 1) {
			throw new IllegalArgumentException(
					String.format("Invalid turn duration %s", playerTurnTotalDurationSeconds));
		}
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getPlayerTurnTotalDurationSeconds() {
		return playerTurnTotalDurationSeconds;
	}
}
