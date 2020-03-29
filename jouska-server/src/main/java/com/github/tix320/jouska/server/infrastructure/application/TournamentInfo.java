package com.github.tix320.jouska.server.infrastructure.application;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tix320.jouska.core.application.game.creation.TournamentSettings;
import com.github.tix320.jouska.core.application.tournament.Tournament;
import com.github.tix320.jouska.core.model.Player;

public class TournamentInfo {

	private final long id;
	private final Player creator;
	private final TournamentSettings tournamentSettings;
	private final Set<Player> registeredPlayers;

	private Tournament tournament;

	public TournamentInfo(long id, Player creator, TournamentSettings tournamentSettings) {
		this.id = id;
		this.creator = creator;
		this.tournamentSettings = tournamentSettings;
		this.registeredPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	public long getId() {
		return id;
	}

	public Player getCreator() {
		return creator;
	}

	public TournamentSettings getTournamentSettings() {
		return tournamentSettings;
	}

	public Set<Player> getRegisteredPlayers() {
		return registeredPlayers;
	}

	public void setTournament(Tournament tournament) {
		Objects.requireNonNull(tournament);
		this.tournament = tournament;
	}

	public Tournament getTournament() {
		if (tournament == null) {
			throw new NoSuchElementException("Tournament does not exists");
		}
		return tournament;
	}
}
