package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.tournament.Tournament;

public interface TournamentSettings {

	String getName();

	int getMaxPlayersCount();

	/**
	 * Create tournament by this settings
	 */
	Tournament createTournament();

	GroupSettings getGroupSettings();

	PlayOffSettings getPlayOffSettings();

	TournamentSettings changeGroupSettings(GroupSettings groupSettings);

	TournamentSettings changePlayOffSettings(PlayOffSettings playOffSettings);
}
