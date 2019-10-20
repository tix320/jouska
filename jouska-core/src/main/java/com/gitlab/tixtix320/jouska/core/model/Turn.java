package com.gitlab.tixtix320.jouska.core.model;

public class Turn {

    private final int x;
    private final int y;

    private Turn() {
        this(-1, -1);
    }

    public Turn(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
