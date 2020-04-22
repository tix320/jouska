package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.model.Player;

public class GameView {

	private final String id;

	private final GameSettingsDto gameSettings;

	private final Player creator;

	private final List<Player> connectedPlayers;

	private final GameState gameState;

	private GameView() {
		this(null, null, null, null, null);
	}

	public GameView(String id, GameSettingsDto gameSettings, Player creator, List<Player> connectedPlayers,
					GameState gameState) {
		this.id = id;
		this.gameSettings = gameSettings;
		this.creator = creator;
		this.connectedPlayers = connectedPlayers;
		this.gameState = gameState;
	}

	public String getId() {
		return id;
	}

	public GameSettingsDto getGameSettings() {
		return gameSettings;
	}

	public Player getCreator() {
		return creator;
	}

	public List<Player> getConnectedPlayers() {
		return connectedPlayers;
	}

	public GameState getGameState() {
		return gameState;
	}
}
