package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.tournament.ClassicTournament;
import com.github.tix320.jouska.core.application.tournament.RestorableTournament;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class ClassicTournamentSettings implements RestorableTournamentSettings {

	private final String name;

	private final int maxPlayersCount;

	private final ClassicGroupSettings groupSettings;

	private final ClassicPlayOffSettings playOffSettings;

	private ClassicTournamentSettings() {
		this(null, -1, null, null);
	}

	public ClassicTournamentSettings(String name, int maxPlayersCount, ClassicGroupSettings groupSettings,
									 ClassicPlayOffSettings playOffSettings) {
		this.name = name;
		this.maxPlayersCount = maxPlayersCount;
		this.groupSettings = groupSettings;
		this.playOffSettings = playOffSettings;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getMaxPlayersCount() {
		return maxPlayersCount;
	}

	@Override
	public RestorableTournament createTournament() {
		return ClassicTournament.create(this);
	}

	@Override
	public ClassicGroupSettings getGroupSettings() {
		return groupSettings;
	}

	@Override
	public ClassicPlayOffSettings getPlayOffSettings() {
		return playOffSettings;
	}

	@Override
	public RestorableTournamentSettings changeGroupSettings(GroupSettings groupSettings) {
		return new ClassicTournamentSettings(name, maxPlayersCount, (ClassicGroupSettings) groupSettings,
				playOffSettings);
	}

	@Override
	public RestorableTournamentSettings changePlayOffSettings(PlayOffSettings playOffSettings) {
		return new ClassicTournamentSettings(name, maxPlayersCount, groupSettings,
				(ClassicPlayOffSettings) playOffSettings);
	}
}
