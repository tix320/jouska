package com.github.tix320.jouska.server.model;

public class TournamentInfo {

	private final long id;
	private final String name;
	private final int playersCount;

	public TournamentInfo(long id, String name, int playersCount) {
		this.id = id;
		this.name = name;
		this.playersCount = playersCount;
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
}
