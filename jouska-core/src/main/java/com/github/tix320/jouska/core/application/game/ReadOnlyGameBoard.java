package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 09-May-20.
 */
public class ReadOnlyGameBoard {

	private final GameBoard gameBoard;

	public ReadOnlyGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	public BoardCell get(int i, int j) {
		return gameBoard.get(i, j);
	}

	public int getHeight() {
		return gameBoard.getHeight();
	}

	public int getWidth() {
		return gameBoard.getWidth();
	}
}
