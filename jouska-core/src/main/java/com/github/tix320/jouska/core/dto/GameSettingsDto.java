package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.SimpleGameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
@JsonSubTypes({
		@JsonSubTypes.Type(value = SimpleGameSettingsDto.class, name = "simple"),
		@JsonSubTypes.Type(value = TimedGameSettingsDto.class, name = "timed")})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface GameSettingsDto {

	String getName();

	BoardType getBoardType();

	int getPlayersCount();

	GameSettings toModel();

	static GameSettingsDto fromModel(GameSettings gameSettings) {
		if (gameSettings instanceof SimpleGameSettings) {
			SimpleGameSettings simpleGameSettings = (SimpleGameSettings) gameSettings;
			return new SimpleGameSettingsDto(simpleGameSettings.getName(), simpleGameSettings.getBoardType(),
					simpleGameSettings.getPlayersCount());
		}
		else if (gameSettings instanceof TimedGameSettings) {
			TimedGameSettings timedGameSettings = (TimedGameSettings) gameSettings;
			return new TimedGameSettingsDto(fromModel(timedGameSettings.getWrappedGameSettings()),
					timedGameSettings.getTurnDurationSeconds(), timedGameSettings.getPlayerTurnTotalDurationSeconds());
		}
		else {
			throw new IllegalArgumentException(gameSettings.getClass().getSimpleName());
		}
	}
}
