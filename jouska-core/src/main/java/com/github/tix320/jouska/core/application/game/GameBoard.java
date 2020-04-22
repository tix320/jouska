package com.github.tix320.jouska.core.application.game;

public final class GameBoard {

	private final BoardCell[][] matrix;

	private GameBoard() {
		this(null);
	}

	public GameBoard(BoardCell[][] matrix) {
		this.matrix = matrix;
	}

	public BoardCell[][] getMatrix() {
		return matrix;
	}

	public BoardCell get(int i, int j) {
		return matrix[i][j];
	}

	public void set(int i, int j, BoardCell cell) {
		matrix[i][j] = cell;
	}

	public int getHeight() {
		return matrix.length;
	}

	public int getWidth() {
		return matrix[0].length;
	}
}
