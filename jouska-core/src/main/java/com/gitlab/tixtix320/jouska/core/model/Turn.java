package com.gitlab.tixtix320.jouska.core.model;

public class Turn {

	private final int x;
	private final int y;
	private final Player currentPlayer;
	private final Player nextPlayer;

	private Turn() {
		this(-1, -1, null, null);
	}

	public Turn(int x, int y) {
		this.x = x;
		this.y = y;
		this.currentPlayer = null;
		this.nextPlayer = null;
	}

	public Turn(int x, int y, Player currentPlayer, Player nextPlayer) {
		this.x = x;
		this.y = y;
		this.currentPlayer = currentPlayer;
		this.nextPlayer = nextPlayer;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public Player getNextPlayer() {
		return nextPlayer;
	}
}
