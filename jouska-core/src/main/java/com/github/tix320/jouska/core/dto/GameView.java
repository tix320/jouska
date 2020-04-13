package com.github.tix320.jouska.core.dto;

import java.util.Set;

import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.model.Player;

public class GameView {

	private final String id;

	private final TimedGameSettings gameSettings;

	private final Player creator;

	private final Set<Player> connectedPlayers;

	private final GameState gameState;

	private GameView() {
		this(null, null, null, null, null);
	}

	public GameView(String id, TimedGameSettings gameSettings, Player creator, Set<Player> connectedPlayers,
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

	public GameSettings getGameSettings() {
		return gameSettings;
	}

	public Player getCreator() {
		return creator;
	}

	public Set<Player> getConnectedPlayers() {
		return connectedPlayers;
	}

	public GameState getGameState() {
		return gameState;
	}
}
