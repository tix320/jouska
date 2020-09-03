package com.github.tix320.jouska.server.infrastructure.application.dbo;

import java.util.Collections;

import com.github.tix320.jouska.core.application.game.creation.*;
import com.github.tix320.jouska.core.application.tournament.Tournament;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public final class DBTournamentSettings implements TournamentSettings {

	private final Player creator;

	private final RestorableTournamentSettings wrappedTournamentSettings;

	public DBTournamentSettings(Player creator, RestorableTournamentSettings wrappedTournamentSettings) {
		this.creator = creator;
		this.wrappedTournamentSettings = wrappedTournamentSettings;
	}

	public Player getCreator() {
		return creator;
	}

	@Override
	public String getName() {
		return wrappedTournamentSettings.getName();
	}

	@Override
	public int getMaxPlayersCount() {
		return wrappedTournamentSettings.getMaxPlayersCount();
	}

	@Override
	public Tournament createTournament() {
		return DBTournament.createNew(this);
	}

	@Override
	public GroupSettings getGroupSettings() {
		return wrappedTournamentSettings.getGroupSettings();
	}

	@Override
	public PlayOffSettings getPlayOffSettings() {
		return wrappedTournamentSettings.getPlayOffSettings();
	}

	@Override
	public TournamentSettings changeGroupSettings(GroupSettings groupSettings) {
		return new DBTournamentSettings(creator, wrappedTournamentSettings.changeGroupSettings(groupSettings));
	}

	@Override
	public TournamentSettings changePlayOffSettings(PlayOffSettings playOffSettings) {
		return new DBTournamentSettings(creator, wrappedTournamentSettings.changePlayOffSettings(playOffSettings));
	}

	public RestorableTournamentSettings getWrappedTournamentSettings() {
		return wrappedTournamentSettings;
	}

	public static DBTournamentSettings wrap(RestorableTournamentSettings settings, Player creator) {
		return new DBTournamentSettings(creator, settings.changeGroupSettings(settings.getGroupSettings()
				.changeBaseGameSettings(new DBGameSettings(creator, Collections.emptySet(),
						(RestorableGameSettings) settings.getGroupSettings().getBaseGameSettings())))
				.changePlayOffSettings(settings.getPlayOffSettings()
						.changeBaseGameSettings(new DBGameSettings(creator, Collections.emptySet(),
								(RestorableGameSettings) settings.getPlayOffSettings().getBaseGameSettings()))));
	}

	public RestorableTournamentSettings extractPureSettings() {
		RestorableGroupSettings groupSettings = wrappedTournamentSettings.getGroupSettings();
		RestorablePlayOffSettings playOffSettings = wrappedTournamentSettings.getPlayOffSettings();

		groupSettings = extractPureSettings(groupSettings);
		playOffSettings = extractPureSettings(playOffSettings);

		return wrappedTournamentSettings.changeGroupSettings(groupSettings).changePlayOffSettings(playOffSettings);
	}

	private static RestorableGroupSettings extractPureSettings(GroupSettings groupSettings) {
		GameSettings baseGameSettings = groupSettings.getBaseGameSettings();
		if (baseGameSettings instanceof DBGameSettings) {
			DBGameSettings dbGameSettings = (DBGameSettings) baseGameSettings;
			RestorableGameSettings wrappedBaseGameSettings = dbGameSettings.getWrappedGameSettings();
			return (RestorableGroupSettings) groupSettings.changeBaseGameSettings(wrappedBaseGameSettings);
		}
		else {
			return (RestorableGroupSettings) groupSettings;
		}
	}

	private static RestorablePlayOffSettings extractPureSettings(PlayOffSettings playOffSettings) {
		GameSettings baseGameSettings = playOffSettings.getBaseGameSettings();
		if (baseGameSettings instanceof DBGameSettings) {
			DBGameSettings dbGameSettings = (DBGameSettings) baseGameSettings;
			RestorableGameSettings wrappedBaseGameSettings = dbGameSettings.getWrappedGameSettings();
			return (RestorablePlayOffSettings) playOffSettings.changeBaseGameSettings(wrappedBaseGameSettings);
		}
		else {
			return (RestorablePlayOffSettings) playOffSettings;
		}
	}
}
