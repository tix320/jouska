package com.gitlab.tixtix320.jouska.core.dto;

import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.Player;

public final class StartGameCommand {

	private final long gameId;

	private final Player myPlayer;

	private final Player firstTurnPlayer;

	private final int playersCount;

	private final GameBoard gameBoard;

	public StartGameCommand() {
		this(-1, null, null, -1, null);
	}

	public StartGameCommand(long gameId, Player myPlayer, Player firstTurnPlayer, int playersCount,
							GameBoard gameBoard) {
		this.gameId = gameId;
		this.myPlayer = myPlayer;
		this.firstTurnPlayer = firstTurnPlayer;
		this.playersCount = playersCount;
		this.gameBoard = gameBoard;
	}

	public long getGameId() {
		return gameId;
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
