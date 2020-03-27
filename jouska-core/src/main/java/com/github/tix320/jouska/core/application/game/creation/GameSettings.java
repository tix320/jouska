package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.BoardType;

public class GameSettings {

	private final String name;

	private final BoardType boardType;

	private final int playersCount;

	protected GameSettings() {
		this.name = null;
		this.boardType = null;
		this.playersCount = -1;
	}

	public GameSettings(String name, BoardType boardType, int playersCount) {
		this.name = name;
		this.boardType = boardType;
		this.playersCount = playersCount;
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Empty name");
		}
		if (!(playersCount > 0 && playersCount <= 4)) {
			throw new IllegalArgumentException(String.format("Invalid players count %s", playersCount));
		}
	}

	public GameSettings(BoardType boardType) {
		this.name = "None";
		this.playersCount = 2;
		this.boardType = boardType;
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
}
