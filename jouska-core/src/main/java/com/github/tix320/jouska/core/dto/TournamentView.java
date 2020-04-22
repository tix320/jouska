package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.model.Player;

public class TournamentView {

	private final String id;

	private final String name;

	private final int playersCount;

	private final int maxPlayersCount;

	private final Player creator;

	private final boolean started;

	public TournamentView() {
		this(null, null, -1, -1, null, false);
	}

	public TournamentView(String id, String name, int playersCount, int maxPlayersCount, Player creator,
						  boolean isStarted) {
		this.id = id;
		this.name = name;
		this.playersCount = playersCount;
		this.maxPlayersCount = maxPlayersCount;
		this.creator = creator;
		this.started = isStarted;
	}

	public String getId() {
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

	public Player getCreator() {
		return creator;
	}

	public boolean isStarted() {
		return started;
	}
}
