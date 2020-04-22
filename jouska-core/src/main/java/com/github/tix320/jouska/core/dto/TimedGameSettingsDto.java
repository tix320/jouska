package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class TimedGameSettingsDto implements GameSettingsDto {

	private final GameSettingsDto wrappedGameSettings;

	private final int turnDurationSeconds;

	private final int playerTurnTotalDurationSeconds;

	public TimedGameSettingsDto() {
		this(null, -1, -1);
	}

	public TimedGameSettingsDto(GameSettingsDto wrappedGameSettings, int turnDurationSeconds,
								int playerTurnTotalDurationSeconds) {
		this.wrappedGameSettings = wrappedGameSettings;
		this.turnDurationSeconds = turnDurationSeconds;
		this.playerTurnTotalDurationSeconds = playerTurnTotalDurationSeconds;
	}

	public GameSettingsDto getWrappedGameSettings() {
		return wrappedGameSettings;
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getPlayerTurnTotalDurationSeconds() {
		return playerTurnTotalDurationSeconds;
	}

	@Override
	@JsonIgnore
	public String getName() {
		return wrappedGameSettings.getName();
	}

	@Override
	@JsonIgnore
	public BoardType getBoardType() {
		return wrappedGameSettings.getBoardType();
	}

	@Override
	@JsonIgnore
	public int getPlayersCount() {
		return wrappedGameSettings.getPlayersCount();
	}

	@Override
	public GameSettings toModel() {
		return new TimedGameSettings(wrappedGameSettings.toModel(), turnDurationSeconds,
				playerTurnTotalDurationSeconds);
	}
}
