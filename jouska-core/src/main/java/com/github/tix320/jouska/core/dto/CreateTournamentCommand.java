package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.game.creation.TournamentSettings;

public class CreateTournamentCommand {

	private final TournamentSettings tournamentSettings;

	public CreateTournamentCommand(TournamentSettings tournamentSettings) {
		this.tournamentSettings = tournamentSettings;
	}

	public TournamentSettings getTournamentSettings() {
		return tournamentSettings;
	}
}
