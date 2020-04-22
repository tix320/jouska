package com.github.tix320.jouska.core.dto;

import java.util.EnumSet;
import java.util.Set;

import com.github.tix320.jouska.core.application.game.GameState;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class GameListFilter {

	public static final GameListFilter EMPTY = new GameListFilter(null, EnumSet.allOf(GameState.class));

	private final String tournamentId;

	private final Set<GameState> states;

	private GameListFilter() {
		this(null, null);
	}

	public GameListFilter(String tournamentId, Set<GameState> states) {
		this.tournamentId = tournamentId;
		this.states = states;
	}

	public String getTournamentId() {
		return tournamentId;
	}

	public Set<GameState> getStates() {
		return states;
	}
}
