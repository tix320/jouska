package com.github.tix320.jouska.core.game;

public final class BoardCell {

	private final PlayerColor color;

	private final int points;

	private BoardCell() {
		this(null, 0);
	}

	public BoardCell(PlayerColor color, int points) {
		this.color = color;
		this.points = points;
	}

	public PlayerColor getColor() {
		return color;
	}

	public int getPoints() {
		return points;
	}

	@Override
	public String toString() {
		return color + "-" + points;
	}
}
