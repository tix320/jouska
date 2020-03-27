package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class PlayerTurn extends GameChange {

	private final CellChange cellChange;

	public PlayerTurn(CellChange cellChange) {
		this.cellChange = cellChange;
	}

	public CellChange getCellChange() {
		return cellChange;
	}
}
