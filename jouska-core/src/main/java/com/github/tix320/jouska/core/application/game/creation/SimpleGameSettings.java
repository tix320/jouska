package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.RestorableGame;
import com.github.tix320.jouska.core.application.game.SimpleGame;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public final class SimpleGameSettings implements RestorableGameSettings {

	private final String name;

	private final BoardType boardType;

	private final int playersCount;

	private SimpleGameSettings() {
		this.name = null;
		this.boardType = null;
		this.playersCount = -1;
	}

	public SimpleGameSettings(String name, BoardType boardType, int playersCount) {
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BoardType getBoardType() {
		return boardType;
	}

	@Override
	public int getPlayersCount() {
		return playersCount;
	}

	@Override
	public RestorableGameSettings changeName(String name) {
		return new SimpleGameSettings(name, getBoardType(), getPlayersCount());
	}

	@Override
	public RestorableGameSettings changePlayersCount(int playersCount) {
		return new SimpleGameSettings(getName(), getBoardType(), playersCount);
	}

	@Override
	public RestorableGame createGame() {
		return SimpleGame.create(this);
	}
}
