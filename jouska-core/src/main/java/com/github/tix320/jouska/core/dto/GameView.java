package com.github.tix320.jouska.core.dto;

import java.util.Set;

import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.model.Player;

public class GameView {

	private final long id;

	private final TimedGameSettings gameSettings;

	private final Player creator;

	private final Set<Player> connectedPlayers;

	private final boolean started;

	private GameView() {
		this(-1, null, null, null, false);
	}

	public GameView(long id, TimedGameSettings gameSettings, Player creator, Set<Player> connectedPlayers,
					boolean isStarted) {
		this.id = id;
		this.gameSettings = gameSettings;
		this.creator = creator;
		this.connectedPlayers = connectedPlayers;
		this.started = isStarted;
	}

	public long getId() {
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

	public boolean isStarted() {
		return started;
	}
}
