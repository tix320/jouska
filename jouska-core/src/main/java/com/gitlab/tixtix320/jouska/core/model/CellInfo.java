package com.gitlab.tixtix320.jouska.core.model;

public final class CellInfo {

    private final int color;

    private final int points;

    private CellInfo() {
        this(0, 0);
    }

    public CellInfo(int color, int points) {
        this.color = color;
        this.points = points;
    }

    public int getColor() {
        return color;
    }

    public int getPoints() {
        return points;
    }
}
