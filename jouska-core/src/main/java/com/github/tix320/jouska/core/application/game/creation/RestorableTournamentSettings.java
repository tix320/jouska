package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.tournament.RestorableTournament;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public interface RestorableTournamentSettings extends TournamentSettings {

	@Override
	RestorableGroupSettings getGroupSettings();

	@Override
	RestorablePlayOffSettings getPlayOffSettings();

	@Override
	RestorableTournamentSettings changeGroupSettings(GroupSettings groupSettings);

	@Override
	RestorableTournamentSettings changePlayOffSettings(PlayOffSettings playOffSettings);

	@Override
	RestorableTournament createTournament();
}
