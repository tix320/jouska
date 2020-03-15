package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.InGamePlayer;
import com.github.tix320.jouska.core.model.PlayerColor;

public final class StartGameCommand {

	private final long gameId;

	private final GameSettings gameSettings;

	private final PlayerColor myPlayer;

	private final List<InGamePlayer> players;

	private final GameBoard gameBoard;

	private StartGameCommand() {
		this(-1, null, null, null, null);
	}

	public StartGameCommand(long gameId, GameSettings gameSettings, PlayerColor myPlayer, List<InGamePlayer> players,
							GameBoard gameBoard) {
		this.gameId = gameId;
		this.gameSettings = gameSettings;
		this.myPlayer = myPlayer;
		this.players = players;
		this.gameBoard = gameBoard;
	}

	public long getGameId() {
		return gameId;
	}

	public GameSettings getGameSettings() {
		return gameSettings;
	}

	public PlayerColor getMyPlayer() {
		return myPlayer;
	}

	public List<InGamePlayer> getPlayers() {
		return players;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}
}
