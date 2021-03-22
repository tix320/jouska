package com.github.tix320.jouska.server.infrastructure.entity;

import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGroupSettings;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
@Entity
public class GroupEntity {

	private RestorableGroupSettings settings;

	@Reference
	private Set<PlayerEntity> players;

	@Reference
	private List<GameEntity> games;

	private GroupEntity() {
	}

	public GroupEntity(RestorableGroupSettings settings, Set<PlayerEntity> players, List<GameEntity> games) {
		this.settings = validateSettings(settings);
		this.players = players;
		this.games = games;
	}

	public RestorableGroupSettings getSettings() {
		return settings;
	}

	public Set<PlayerEntity> getPlayers() {
		return players;
	}

	public List<GameEntity> getGames() {
		return games;
	}

	private static RestorableGroupSettings validateSettings(RestorableGroupSettings settings) {
		GameSettings baseGameSettings = settings.getBaseGameSettings();
		if (!(baseGameSettings instanceof RestorableGameSettings)) {
			throw new IllegalStateException();
		}
		return settings;
	}
}
