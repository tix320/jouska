package com.gitlab.tixtix320.jouska.core.model;

public final class GameBoard {

    private final CellInfo[][] matrix;

    public GameBoard(CellInfo[][] matrix) {
        this.matrix = matrix;
    }

    public CellInfo[][] getMatrix() {
        return matrix;
    }
}
