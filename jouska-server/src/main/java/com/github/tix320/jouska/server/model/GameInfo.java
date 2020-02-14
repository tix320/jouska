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
	private final Map<Long, Player> playersById;
	private final CellInfo[][] board;
	private final JouskaGame jouskaGame;
	private final int turnDurationSeconds;
	private final int gameDurationMinutes;

	public GameInfo(long id, String name, Set<Long> playerIds, Player[] players, Map<Long, Player> playersById,
					CellInfo[][] board, JouskaGame jouskaGame, int turnDurationSeconds, int gameDurationMinutes) {
		this.id = id;
		this.name = name;
		this.playerIds = playerIds;
		this.players = players;
		this.playersById = playersById;
		this.board = board;
		this.jouskaGame = jouskaGame;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
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

	public void setPlayerById(Long clientId, Player player) {
		playersById.put(clientId, player);
	}

	public Player getPlayerById(Long clientId) {
		return playersById.get(clientId);
	}

	public Long getPlayerId(Player player) {
		for (Long playerId : playerIds) {
			if (getPlayerById(playerId) == player) {
				return playerId;
			}
		}
		throw new IllegalArgumentException(String.format("Player %s not found", player));
	}

	public CellInfo[][] getBoard() {
		return board;
	}

	public int getTurnDurationSeconds() {
		return turnDurationSeconds;
	}

	public int getGameDurationMinutes() {
		return gameDurationMinutes;
	}
}
