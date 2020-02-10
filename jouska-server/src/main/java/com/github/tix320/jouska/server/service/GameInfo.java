package com.github.tix320.jouska.server.service;

import java.util.Map;
import java.util.Set;

import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.Player;

public final class GameInfo {

	private final long id;
	private final String name;
	private final Set<Long> playerIds;
	private final Player[] players;
	private final Map<Long, Integer> playerIndexById;
	private final JouskaGame jouskaGame;

	private GameInfo() {
		this(-1, null, null, null, null, null);
	}

	public GameInfo(long id, String name, Set<Long> playerIds, Player[] players, Map<Long, Integer> playerIndexById,
					JouskaGame jouskaGame) {
		this.id = id;
		this.name = name;
		this.playerIds = playerIds;
		this.players = players;
		this.playerIndexById = playerIndexById;
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

	public Player getPlayer(long id) {
		return players[playerIndexById.get(id)];
	}

	public Player[] getPlayers() {
		return players;
	}

	public JouskaGame getGame() {
		return jouskaGame;
	}

	public void addPlayer(long id, int playerIndex) {
		playerIds.add(id);
		playerIndexById.put(id, playerIndex);
	}
}
