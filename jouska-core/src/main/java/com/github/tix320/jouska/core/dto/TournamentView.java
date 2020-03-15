package com.github.tix320.jouska.core.dto;

public class TournamentView {

	private final long id;

	private final String name;

	private final int playersCount;

	public TournamentView() {
		this(-1, null, -1);
	}

	public TournamentView(long id, String name, int playersCount) {
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
