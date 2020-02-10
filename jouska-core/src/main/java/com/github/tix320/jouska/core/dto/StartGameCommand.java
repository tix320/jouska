package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;

public final class StartGameCommand {

	private final long gameId;

	private final String name;

	private final Player myPlayer;

	private final Player[] players;

	private final GameBoard gameBoard;

	private StartGameCommand() {
		this(-1, null, null, null, null);
	}

	public StartGameCommand(long gameId, String name, Player myPlayer, Player[] players, GameBoard gameBoard) {
		this.gameId = gameId;
		this.name = name;
		this.myPlayer = myPlayer;
		this.players = players;
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

	public Player[] getPlayers() {
		return players;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}
}
