package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayerTimedTurn extends PlayerTurn {

	private final long remainingTurnMillis;

	private final long remainingPlayerTotalTurnMillis;

	private PlayerTimedTurn() {
		this(null, -1, -1);
	}

	public PlayerTimedTurn(Point point, long remainingTurnMillis, long remainingPlayerTotalTurnMillis) {
		super(point);
		this.remainingTurnMillis = remainingTurnMillis;
		this.remainingPlayerTotalTurnMillis = remainingPlayerTotalTurnMillis;
	}

	public long getRemainingTurnMillis() {
		return remainingTurnMillis;
	}

	public long getRemainingPlayerTotalTurnMillis() {
		return remainingPlayerTotalTurnMillis;
	}

	@Override
	public void accept(GameChangeVisitor visitor) {
		visitor.visit(this);
	}
}
