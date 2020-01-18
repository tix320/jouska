package com.github.tix320.jouska.core.model;

public class Turn {

	private final int number;
	private final int i;
	private final int j;
	private final Player currentPlayer;
	private final Player nextPlayer;

	private Turn() {
		this(-1, -1, null, null);
	}

	public Turn(int i, int j) {
		this(i, j, null, null);
	}

	public Turn(int i, int j, Player currentPlayer, Player nextPlayer) {
		this(-1, i, j, currentPlayer, nextPlayer);
	}

	public Turn(int number, int i, int j, Player currentPlayer, Player nextPlayer) {
		this.number = number;
		this.i = i;
		this.j = j;
		this.currentPlayer = currentPlayer;
		this.nextPlayer = nextPlayer;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public Player getNextPlayer() {
		return nextPlayer;
	}

	public Turn changeNumber(int number) {
		return new Turn(number, this.i, this.j, this.currentPlayer, this.nextPlayer);
	}
}
