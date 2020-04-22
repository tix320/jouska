package com.github.tix320.jouska.core.application.game;

import java.util.List;

public class PlayerWithPoints {
	private final GamePlayer player;
	private final List<Point> points;

	private PlayerWithPoints() {
		this(null, null);
	}

	public PlayerWithPoints(GamePlayer player, List<Point> points) {
		this.player = player;
		this.points = points;
	}

	public GamePlayer getPlayer() {
		return player;
	}

	public List<Point> getPoints() {
		return points;
	}
}
