package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class GameWatchDto {

	private final String gameId;

	private final TimedGameSettings gameSettings;

	private final List<InGamePlayer> players;

	protected GameWatchDto() {
		this(null, null, null);
	}

	public GameWatchDto(String gameId, TimedGameSettings gameSettings, List<InGamePlayer> players) {
		this.gameId = gameId;
		this.gameSettings = gameSettings;
		this.players = players;
	}

	public String getGameId() {
		return gameId;
	}

	public TimedGameSettings getGameSettings() {
		return gameSettings;
	}

	public List<InGamePlayer> getPlayers() {
		return players;
	}
}
