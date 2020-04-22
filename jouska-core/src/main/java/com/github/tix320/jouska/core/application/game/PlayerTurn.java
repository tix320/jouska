package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class PlayerTurn implements GameChange {

	private final Point point;

	private PlayerTurn() {
		this(null);
	}

	public PlayerTurn(Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}

	@Override
	public void accept(GameChangeVisitor visitor) {
		visitor.visit(this);
	}
}
