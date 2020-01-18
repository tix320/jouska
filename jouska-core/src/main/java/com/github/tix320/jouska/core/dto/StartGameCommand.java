package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;

public final class StartGameCommand {

	private final long gameId;

	private final String name;

	private final Player myPlayer;

	private final Player firstTurnPlayer;

	private final int playersCount;

	private final GameBoard gameBoard;

	public StartGameCommand() {
		this(-1, null, null, null, -1, null);
	}

	public StartGameCommand(long gameId, String name, Player myPlayer, Player firstTurnPlayer, int playersCount,
							GameBoard gameBoard) {
		this.gameId = gameId;
		this.name = name;
		this.myPlayer = myPlayer;
		this.firstTurnPlayer = firstTurnPlayer;
		this.playersCount = playersCount;
		this.gameBoard = gameBoard;
	}

	public long getGameId() {
		return gameId;
	}

	public String getName() {
		return name;
	}

	public Player getMyPlayer() {
		return myPlayer;
	}

	public Player getFirstTurnPlayer() {
		return firstTurnPlayer;
	}

	public int getPlayersCount() {
		return playersCount;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}
}
