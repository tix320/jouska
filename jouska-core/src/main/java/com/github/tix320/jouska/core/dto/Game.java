package com.github.tix320.jouska.core.dto;

public class Game {

	private final long id;

	private final String name;

	private final int playersCount;

	private final int maxPlayersCount;

	private Game() {
		this(-1, null, -1, -1);
	}

	public Game(long id, String name, int playersCount, int maxPlayersCount) {
		this.id = id;
		this.name = name;
		this.playersCount = playersCount;
		this.maxPlayersCount = maxPlayersCount;
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
}
