package com.github.tix320.jouska.server.model;

import java.util.Set;

import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.Player;

public final class GameInfo {

	private final long id;
	private final String name;
	private final Set<Long> playerIds;
	private final Player[] players;
	private final JouskaGame jouskaGame;

	private GameInfo() {
		this(-1, null, null, null, null);
	}

	public GameInfo(long id, String name, Set<Long> playerIds, Player[] players, JouskaGame jouskaGame) {
		this.id = id;
		this.name = name;
		this.playerIds = playerIds;
		this.players = players;
		this.jouskaGame = jouskaGame;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<Long> getPlayerIds() {
		return playerIds;
	}

	public Player[] getPlayers() {
		return players;
	}

	public JouskaGame getGame() {
		return jouskaGame;
	}
}
