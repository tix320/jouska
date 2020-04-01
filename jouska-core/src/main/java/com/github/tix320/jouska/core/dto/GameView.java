package com.github.tix320.jouska.core.dto;

import java.util.Set;

import com.github.tix320.jouska.core.model.Player;

public class GameView {

	private final long id;

	private final String name;

	private final int playersCount;

	private final int maxPlayersCount;

	private final int turnDurationSeconds;

	private final int playerTurTotalDurationSeconds;

	private final Player creator;

	private final Set<Player> accessedPlayers;

	private GameView() {
		this(-1, null, -1, -1, -1, -1, null, null);
	}

	public GameView(long id, String name, int playersCount, int maxPlayersCount, int turnDurationSeconds,
					int playerTurTotalDurationSeconds, Player creator, Set<Player> accessedPlayers) {
		this.id = id;
		this.name = name;
		this.playersCount = playersCount;
		this.maxPlayersCount = maxPlayersCount;
		this.turnDurationSeconds = turnDurationSeconds;
		this.playerTurTotalDurationSeconds = playerTurTotalDurationSeconds;
		this.creator = creator;
		this.accessedPlayers = accessedPlayers;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public int getMaxPlayersCount() {
		return maxPlayersCount;
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getPlayerTurTotalDurationSeconds() {
		return playerTurTotalDurationSeconds;
	}

	public Player getCreator() {
		return creator;
	}

	public Set<Player> getAccessedPlayers() {
		return accessedPlayers;
	}
}
