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
}
