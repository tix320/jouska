package com.github.tix320.jouska.core.model;

public final class CellInfo {

	private final Player player;

	private final int points;

	private CellInfo() {
		this(null, 0);
	}

	public CellInfo(Player player, int points) {
		this.player = player;
		this.points = points;
	}

	public Player getPlayer() {
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
