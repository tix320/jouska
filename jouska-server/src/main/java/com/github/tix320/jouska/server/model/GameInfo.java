package com.github.tix320.jouska.server.model;

import java.util.Map;
import java.util.Set;

import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.Player;

public final class GameInfo {

	private final long id;
	private final String name;
	private final Set<Long> playerIds;
	private final Player[] players;
	private final Map<Long, Player> playerByClientId;
	private final CellInfo[][] board;
	private final JouskaGame jouskaGame;

	public GameInfo(long id, String name, Set<Long> playerIds, Player[] players, Map<Long, Player> playerByClientId,
					CellInfo[][] board, JouskaGame jouskaGame) {
		this.id = id;
		this.name = name;
		this.playerIds = playerIds;
		this.players = players;
		this.playerByClientId = playerByClientId;
		this.board = board;
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

	public void setPlayerByClientId(Long clientId, Player player) {
		playerByClientId.put(clientId, player);
	}

	public Player getPlayerByClientId(Long clientId) {
		return playerByClientId.get(clientId);
	}

	public CellInfo[][] getBoard() {
		return board;
	}
}
