package com.github.tix320.jouska.server.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.Player;

public final class GameInfo {

	private final long id;
	private final GameSettings settings;
	private Game game;
	private Set<Player> connectedPlayers;

	public GameInfo(long id, GameSettings settings) {
		this.id = id;
		this.settings = settings;
		this.connectedPlayers = new HashSet<>();
	}

	public long getId() {
		return id;
	}

	public GameSettings getSettings() {
		return settings;
	}

	public Set<Player> getConnectedPlayers() {
		return connectedPlayers;
	}

	public void setGame(Game game) {
		if (game == null) {
			throw new IllegalArgumentException("Game must not be null");
		}
		this.game = game;
	}

	public Game getGame() {
		if (game == null) {
			throw new NoSuchElementException("Game does not exists");
		}
		return game;
	}
}
