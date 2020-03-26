package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.game.GameBoard;
import com.github.tix320.jouska.core.game.PlayerColor;

public final class WatchGameCommand {

	private final long gameId;
	private final String name;
	private final PlayerColor[] players;
	private final GameBoard initialGameBoard;

	public WatchGameCommand() {
		this(-1, null, null, null);
	}

	public WatchGameCommand(long gameId, String name, PlayerColor[] players, GameBoard initialGameBoard) {
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

	public PlayerColor[] getPlayers() {
		return players;
	}

	public GameBoard getInitialGameBoard() {
		return initialGameBoard;
	}
}
