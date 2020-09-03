package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.application.game.creation.ClassicGroupSettings;
import com.github.tix320.jouska.core.application.game.creation.ClassicPlayOffSettings;
import com.github.tix320.jouska.core.application.game.creation.ClassicTournamentSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class ClassicTournamentSettingsDto {

	private final String name;

	private final int maxPlayersCount;

	private final GameSettingsDto groupGameSettings;

	private final GameSettingsDto playOffGameSettings;

	public ClassicTournamentSettingsDto() {
		this.name = null;
		this.maxPlayersCount = -1;
		this.groupGameSettings = null;
		this.playOffGameSettings = null;
	}

	public ClassicTournamentSettingsDto(String name, int maxPlayersCount, GameSettingsDto groupGameSettings,
										GameSettingsDto playOffGameSettings) {
		this.name = name;
		this.maxPlayersCount = maxPlayersCount;
		this.groupGameSettings = groupGameSettings;
		this.playOffGameSettings = playOffGameSettings;

		if (groupGameSettings.getClass() != playOffGameSettings.getClass()) {
			throw new IllegalArgumentException("Must be same type");
		}
	}

	public String getName() {
		return name;
	}

	public int getMaxPlayersCount() {
		return maxPlayersCount;
	}

	public GameSettingsDto getGroupGameSettings() {
		return groupGameSettings;
	}

	public GameSettingsDto getPlayOffGameSettings() {
		return playOffGameSettings;
	}

	public ClassicTournamentSettings toModel() {
		return new ClassicTournamentSettings(name, maxPlayersCount,
				new ClassicGroupSettings(groupGameSettings.toModel()),
				new ClassicPlayOffSettings(playOffGameSettings.toModel()));
	}

	public static ClassicTournamentSettingsDto fromModel(ClassicTournamentSettings tournamentSettings) {
		return new ClassicTournamentSettingsDto(tournamentSettings.getName(), tournamentSettings.getMaxPlayersCount(),
				GameSettingsDto.fromModel(tournamentSettings.getGroupSettings().getBaseGameSettings()),
				GameSettingsDto.fromModel(tournamentSettings.getPlayOffSettings().getBaseGameSettings()));
	}
}
