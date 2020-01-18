package com.gitlab.tixtix320.jouska.core.dto;

import java.util.List;

import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.Player;
import com.gitlab.tixtix320.jouska.core.model.Turn;

public final class WatchGameCommand {

	private final long gameId;
	private final String name;
	private final Player firstTurnPlayer;
	private final int playersCount;
	private final GameBoard initialGameBoard;
	private final List<Turn> turns;

	public WatchGameCommand() {
		this(-1, null, null, -1, null, null);
	}

	public WatchGameCommand(long gameId, String name, Player firstTurnPlayer, int playersCount,
							GameBoard initialGameBoard, List<Turn> turns) {
		this.gameId = gameId;
		this.name = name;
		this.firstTurnPlayer = firstTurnPlayer;
		this.playersCount = playersCount;
		this.initialGameBoard = initialGameBoard;
		this.turns = turns;
	}

	public long getGameId() {
		return gameId;
	}

	public String getName() {
		return name;
	}

	public Player getFirstTurnPlayer() {
		return firstTurnPlayer;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public GameBoard getInitialGameBoard() {
		return initialGameBoard;
	}

	public List<Turn> getTurns() {
		return turns;
	}
}
