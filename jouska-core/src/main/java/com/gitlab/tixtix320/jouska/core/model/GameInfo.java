package com.gitlab.tixtix320.jouska.core.model;

public final class GameInfo {

    private final long id;
    private final String name;
    private final int players;
    private final int maxPlayers;

    public GameInfo(long id, String name, int players, int maxPlayers) {
        this.id = id;
        this.name = name;
        this.players = players;
        this.maxPlayers = maxPlayers;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
