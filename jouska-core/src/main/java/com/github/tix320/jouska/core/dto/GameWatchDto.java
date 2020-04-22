package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.application.game.GamePlayer;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class GameWatchDto {

	private final String gameId;

	private final GameSettingsDto gameSettings;

	private final List<GamePlayer> players;

	protected GameWatchDto() {
		this(null, null, null);
	}

	public GameWatchDto(String gameId, GameSettingsDto gameSettings, List<GamePlayer> players) {
		this.gameId = gameId;
		this.gameSettings = gameSettings;
		this.players = players;
	}

	public String getGameId() {
		return gameId;
	}

	public GameSettingsDto getGameSettings() {
		return gameSettings;
	}

	public List<GamePlayer> getPlayers() {
		return players;
	}
}
