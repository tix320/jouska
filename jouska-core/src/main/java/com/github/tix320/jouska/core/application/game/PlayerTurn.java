package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class PlayerTurn implements GameChange {

	private final CellChange cellChange;

	private PlayerTurn() {
		this(null);
	}

	public PlayerTurn(CellChange cellChange) {
		this.cellChange = cellChange;
	}

	public CellChange getCellChange() {
		return cellChange;
	}
}
