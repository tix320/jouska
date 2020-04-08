package com.github.tix320.jouska.core.application.game.creation;

import java.util.Set;

import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.model.Player;

public class GameSettings {

	private final String name;

	private final BoardType boardType;

	private final int playersCount;

	private final Set<Player> accessedPlayers;

	protected GameSettings() {
		this.name = null;
		this.boardType = null;
		this.playersCount = -1;
		this.accessedPlayers = null;
	}

	public GameSettings(String name, BoardType boardType, int playersCount, Set<Player> accessedPlayers) {
		this.name = name;
		this.boardType = boardType;
		this.playersCount = playersCount;
		this.accessedPlayers = Set.copyOf(accessedPlayers);
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Empty name");
		}
		if (!(playersCount > 0 && playersCount <= 4)) {
			throw new IllegalArgumentException(String.format("Invalid players count %s", playersCount));
		}
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

	public Set<Player> getAccessedPlayers() {
		return accessedPlayers;
	}

	public GameSettings changeName(String name) {
		return new GameSettings(name, boardType, playersCount, accessedPlayers);
	}

	public GameSettings changePlayersCount(int playersCount) {
		return new GameSettings(name, boardType, playersCount, accessedPlayers);
	}
}
