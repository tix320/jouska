package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.TimedGame;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class TimedGameSettings implements RestorableGameSettings {

	private final GameSettings wrappedGameSettings;

	private final int turnDurationSeconds;

	private final int playerTurnTotalDurationSeconds;

	private TimedGameSettings() {
		this.wrappedGameSettings = null;
		this.turnDurationSeconds = -1;
		this.playerTurnTotalDurationSeconds = -1;
	}

	public TimedGameSettings(GameSettings wrappedGameSettings, int turnDurationSeconds,
							 int playerTurnTotalDurationSeconds) {
		this.wrappedGameSettings = wrappedGameSettings;
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

	public GameSettings getWrappedGameSettings() {
		return wrappedGameSettings;
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getPlayerTurnTotalDurationSeconds() {
		return playerTurnTotalDurationSeconds;
	}

	@Override
	public String getName() {
		return wrappedGameSettings.getName();
	}

	@Override
	public BoardType getBoardType() {
		return wrappedGameSettings.getBoardType();
	}

	@Override
	public int getPlayersCount() {
		return wrappedGameSettings.getPlayersCount();
	}

	@Override
	public TimedGameSettings changeName(String name) {
		return new TimedGameSettings(wrappedGameSettings.changeName(name), turnDurationSeconds,
				playerTurnTotalDurationSeconds);
	}

	@Override
	public TimedGameSettings changePlayersCount(int playersCount) {
		return new TimedGameSettings(wrappedGameSettings.changePlayersCount(playersCount), turnDurationSeconds,
				playerTurnTotalDurationSeconds);
	}

	@Override
	public TimedGame createGame() {
		return TimedGame.create(this);
	}
}
