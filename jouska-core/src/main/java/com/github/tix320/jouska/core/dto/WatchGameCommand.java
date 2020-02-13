package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;

public final class WatchGameCommand {

	private final long gameId;
	private final String name;
	private final Player[] players;
	private final GameBoard initialGameBoard;

	public WatchGameCommand() {
		this(-1, null, null, null);
	}

	public WatchGameCommand(long gameId, String name, Player[] players, GameBoard initialGameBoard) {
		this.gameId = gameId;
		this.name = name;
		this.players = players;
		this.initialGameBoard = initialGameBoard;
	}

	public long getGameId() {
		return gameId;
	}

	public String getName() {
		return name;
	}

	public Player[] getPlayers() {
		return players;
	}

	public GameBoard getInitialGameBoard() {
		return initialGameBoard;
	}
}
