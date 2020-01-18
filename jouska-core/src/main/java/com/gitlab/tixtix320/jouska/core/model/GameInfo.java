package com.gitlab.tixtix320.jouska.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class GameInfo {

	private final long id;
	private final String name;
	private final Set<Long> players;
	private final Player firstTurnPlayer;
	private final int maxPlayers;
	private final GameBoard initialGameBoard;
	private final List<Turn> turns;

	private GameInfo() {
		this(-1, null, Collections.emptySet(), null, -1, null, null);
	}

	public GameInfo(long id, String name, Set<Long> players, Player firstTurnPlayer, int maxPlayers,
					GameBoard initialGameBoard, List<Turn> turns) {
		this.id = id;
		this.name = name;
		this.players = players;
		this.firstTurnPlayer = firstTurnPlayer;
		this.maxPlayers = maxPlayers;
		this.initialGameBoard = initialGameBoard;
		this.turns = turns;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<Long> getPlayers() {
		return players;
	}

	public Player getFirstTurnPlayer() {
		return firstTurnPlayer;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public GameBoard getInitialGameBoard() {
		return initialGameBoard;
	}

	public void addTurn(Turn turn) {
		turns.add(turn);
	}

	public List<Turn> getTurns() {
		return new ArrayList<>(turns);
	}
}
