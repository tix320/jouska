package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;

public final class WatchGameCommand {

	private final long gameId;
	private final String name;
	private final Player[] players;
	private final GameBoard initialGameBoard;
	private final List<Point> turns;

	public WatchGameCommand() {
		this(-1, null, null, null, null);
	}

	public WatchGameCommand(long gameId, String name, Player[] players, GameBoard initialGameBoard, List<Point> turns) {
		this.gameId = gameId;
		this.name = name;
		this.players = players;
		this.initialGameBoard = initialGameBoard;
		this.turns = turns;
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

	public List<Point> getTurns() {
		return turns;
	}
}
