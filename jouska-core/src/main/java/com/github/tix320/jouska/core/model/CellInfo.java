package com.github.tix320.jouska.core.model;

public final class CellInfo {

	private final PlayerColor player;

	private final int points;

	private CellInfo() {
		this(null, 0);
	}

	public CellInfo(PlayerColor player, int points) {
		this.player = player;
		this.points = points;
	}

	public PlayerColor getPlayer() {
		return player;
	}

	public int getPoints() {
		return points;
	}

	@Override
	public String toString() {
		return player + "-" + points;
	}
}
