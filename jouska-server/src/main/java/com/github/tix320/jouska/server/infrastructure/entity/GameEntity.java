package com.github.tix320.jouska.server.infrastructure.entity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.tix320.jouska.core.application.game.GameChange;
import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

@Entity("games")
public class GameEntity implements Identifiable {

	@Id
	private ObjectId id;

	@Reference
	private PlayerEntity creator;

	private Set<PlayerEntity> accessedPlayers;

	private RestorableGameSettings settings;

	private GameState state;

	private List<GamePlayerEntity> players;

	private List<GameChange> changes;

	GameStatisticsEntity statistics;

	private GameEntity() {
	}

	public GameEntity(String id) {
		this.id = new ObjectId(id);
	}

	public GameEntity(PlayerEntity creator, Set<PlayerEntity> accessedPlayers, RestorableGameSettings settings,
					  GameState state, List<GamePlayerEntity> players, List<GameChange> changes,
					  GameStatisticsEntity statistics) {
		this.creator = creator;
		this.accessedPlayers = accessedPlayers;
		this.settings = settings;
		this.state = state;
		this.players = players;
		this.changes = changes;
		this.statistics = statistics;
	}

	public void setCreator(PlayerEntity creator) {
		this.creator = creator;
	}

	public void setAccessedPlayers(Set<PlayerEntity> accessedPlayers) {
		this.accessedPlayers = accessedPlayers;
	}

	public void setSettings(RestorableGameSettings settings) {
		this.settings = settings;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public void setPlayers(List<GamePlayerEntity> players) {
		this.players = players;
	}

	public void setChanges(List<GameChange> changes) {
		this.changes = changes;
	}

	public void setStatistics(GameStatisticsEntity statistics) {
		this.statistics = statistics;
	}

	public String getId() {
		return id.toHexString();
	}

	public PlayerEntity getCreator() {
		return creator;
	}

	public Set<PlayerEntity> getAccessedPlayers() {
		return Objects.requireNonNullElse(accessedPlayers, Collections.emptySet());
	}

	public RestorableGameSettings getSettings() {
		return settings;
	}

	public GameState getState() {
		return state;
	}

	public List<GamePlayerEntity> getPlayers() {
		if (players == null) {
			return Collections.emptyList();
		}
		return players;
	}

	public List<GameChange> getChanges() {
		return changes;
	}

	public GameStatisticsEntity getStatistics() {
		return statistics;
	}
}
