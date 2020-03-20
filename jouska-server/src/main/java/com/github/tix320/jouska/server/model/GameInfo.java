package com.github.tix320.jouska.server.model;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.Player;

public final class GameInfo {

	private final long id;
	private final GameSettings settings;
	private final Lock gameLock;
	private Game game;
	private final Set<Player> connectedPlayers;
	private final Set<Player> players;

	public GameInfo(long id, GameSettings settings) {
		this.id = id;
		this.settings = settings;
		this.connectedPlayers = new HashSet<>();
		this.players = new HashSet<>();
		this.gameLock = new ReentrantLock();
	}

	public long getId() {
		return id;
	}

	public GameSettings getSettings() {
		return settings;
	}

	public Lock getGameLock() {
		return gameLock;
	}

	public Set<Player> getConnectedPlayers() {
		return connectedPlayers;
	}

	public Set<Player> getPlayers() {
		return players;
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
