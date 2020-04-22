package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.SimpleGameSettings;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class SimpleGameSettingsDto implements GameSettingsDto {

	private final String name;

	private final BoardType boardType;

	private final int playersCount;

	private SimpleGameSettingsDto() {
		this(null, null, -1);
	}

	public SimpleGameSettingsDto(String name, BoardType boardType, int playersCount) {
		this.name = name;
		this.boardType = boardType;
		this.playersCount = playersCount;
	}

	public String getName() {
		return name;
	}

	public BoardType getBoardType() {
		return boardType;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	@Override
	public GameSettings toModel() {
		return new SimpleGameSettings(name, boardType, playersCount);
	}
}
