package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

public final class StartGameCommand {

	private final long gameId;

	private final TimedGameSettings gameSettings;

	private final PlayerColor myPlayer;

	private final List<InGamePlayer> players;

	private StartGameCommand() {
		this(-1, null, null, null);
	}

	public StartGameCommand(long gameId, TimedGameSettings gameSettings, PlayerColor myPlayer,
							List<InGamePlayer> players) {
		this.gameId = gameId;
		this.gameSettings = gameSettings;
		this.myPlayer = myPlayer;
		this.players = players;
	}

	public long getGameId() {
		return gameId;
	}

	public TimedGameSettings getGameSettings() {
		return gameSettings;
	}

	public PlayerColor getMyPlayer() {
		return myPlayer;
	}

	public List<InGamePlayer> getPlayers() {
		return players;
	}
}
