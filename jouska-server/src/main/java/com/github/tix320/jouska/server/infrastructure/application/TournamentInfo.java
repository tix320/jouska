package com.github.tix320.jouska.server.infrastructure.application;

import com.github.tix320.jouska.core.application.game.creation.TournamentSettings;
import com.github.tix320.jouska.core.application.tournament.Tournament;

public class TournamentInfo {

	private final long id;
	private final TournamentSettings tournamentSettings;
	private final Tournament tournament;

	public TournamentInfo(long id, TournamentSettings tournamentSettings, Tournament tournament) {
		this.id = id;
		this.tournamentSettings = tournamentSettings;
		this.tournament = tournament;
	}

	public long getId() {
		return id;
	}

	public TournamentSettings getTournamentSettings() {
		return tournamentSettings;
	}

	public Tournament getTournament() {
		return tournament;
	}
}
