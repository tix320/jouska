package com.github.tix320.jouska.core.dto;

public class CreateTournamentCommand {

	private final ClassicTournamentSettingsDto tournamentSettings;

	private CreateTournamentCommand() {
		this(null);
	}

	public CreateTournamentCommand(ClassicTournamentSettingsDto tournamentSettings) {
		this.tournamentSettings = tournamentSettings;
	}

	public ClassicTournamentSettingsDto getTournamentSettings() {
		return tournamentSettings;
	}
}
