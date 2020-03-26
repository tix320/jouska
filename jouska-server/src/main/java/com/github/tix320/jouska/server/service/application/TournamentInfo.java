package com.github.tix320.jouska.server.service.application;

import com.github.tix320.jouska.core.game.creation.TournamentSettings;

public class TournamentInfo {

	private final long id;
	private final TournamentSettings tournamentSettings;

	public TournamentInfo(long id, TournamentSettings tournamentSettings) {
		this.id = id;
		this.tournamentSettings = tournamentSettings;
	}

	public long getId() {
		return id;
	}

	public TournamentSettings getTournamentSettings() {
		return tournamentSettings;
	}
}
