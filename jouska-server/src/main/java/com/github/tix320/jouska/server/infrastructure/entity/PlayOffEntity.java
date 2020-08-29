package com.github.tix320.jouska.server.infrastructure.entity;

import java.util.List;

import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorablePlayOffSettings;
import dev.morphia.annotations.Reference;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayOffEntity {

	private RestorablePlayOffSettings settings;

	@Reference
	private List<PlayerEntity> players;

	private List<List<PlayOffGameEntity>> tours;

	private PlayOffEntity() {
	}

	public PlayOffEntity(RestorablePlayOffSettings settings, List<PlayerEntity> players,
						 List<List<PlayOffGameEntity>> tours) {
		this.settings = validateSettings(settings);
		this.players = players;
		this.tours = tours;
	}

	public RestorablePlayOffSettings getSettings() {
		return settings;
	}

	public List<PlayerEntity> getPlayers() {
		return players;
	}

	public List<List<PlayOffGameEntity>> getTours() {
		return tours;
	}

	private static RestorablePlayOffSettings validateSettings(RestorablePlayOffSettings settings) {
		GameSettings baseGameSettings = settings.getBaseGameSettings();
		if (!(baseGameSettings instanceof RestorableGameSettings)) {
			throw new IllegalStateException();
		}
		return settings;
	}
}
