package com.github.tix320.jouska.core.game;

import java.util.List;

public class PlayerWithPoints {
	private final InGamePlayer player;
	private final List<Point> points;

	private PlayerWithPoints() {
		this(null, null);
	}

	public PlayerWithPoints(InGamePlayer player, List<Point> points) {
		this.player = player;
		this.points = points;
	}

	public InGamePlayer getPlayer() {
		return player;
	}

	public List<Point> getPoints() {
		return points;
	}
}
