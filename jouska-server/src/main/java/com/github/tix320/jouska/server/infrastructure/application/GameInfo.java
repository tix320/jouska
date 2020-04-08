package com.github.tix320.jouska.server.infrastructure.application;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.Player;

public final class GameInfo {

	private final long id;
	private final GameSettings settings;
	private Game game;
	private final Player creator;
	private final Set<Player> connectedPlayers;

	public GameInfo(long id, GameSettings settings, Player creator) {
		this.id = id;
		this.settings = settings;
		this.creator = creator;
		this.connectedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	public long getId() {
		return id;
	}

	public GameSettings getSettings() {
		return settings;
	}

	public Player getCreator() {
		return creator;
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

	public Optional<Game> getGame() {
		return Optional.ofNullable(game);
	}
}
