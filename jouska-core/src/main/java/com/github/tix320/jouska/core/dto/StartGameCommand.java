package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.game.creation.GameSettings;
import com.github.tix320.jouska.core.game.InGamePlayer;
import com.github.tix320.jouska.core.game.PlayerColor;

public final class StartGameCommand {

	private final long gameId;

	private final GameSettings gameSettings;

	private final PlayerColor myPlayer;

	private final List<InGamePlayer> players;

	private StartGameCommand() {
		this(-1, null, null, null);
	}

	public StartGameCommand(long gameId, GameSettings gameSettings, PlayerColor myPlayer, List<InGamePlayer> players) {
		this.gameId = gameId;
		this.gameSettings = gameSettings;
		this.myPlayer = myPlayer;
		this.players = players;
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
}
