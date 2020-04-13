package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayerTimedTurn extends PlayerTurn implements TimedGameChange {

	private final long remainingTurnMillis;

	private final long remainingPlayerTotalTurnMillis;

	private PlayerTimedTurn() {
		this(null, -1, -1);
	}

	public PlayerTimedTurn(CellChange cellChange, long remainingTurnMillis, long remainingPlayerTotalTurnMillis) {
		super(cellChange);
		this.remainingTurnMillis = remainingTurnMillis;
		this.remainingPlayerTotalTurnMillis = remainingPlayerTotalTurnMillis;
	}

	public long getRemainingTurnMillis() {
		return remainingTurnMillis;
	}

	public long getRemainingPlayerTotalTurnMillis() {
		return remainingPlayerTotalTurnMillis;
	}
}
