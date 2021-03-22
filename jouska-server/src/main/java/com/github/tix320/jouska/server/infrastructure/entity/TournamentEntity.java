package com.github.tix320.jouska.server.infrastructure.entity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableTournamentSettings;
import com.github.tix320.jouska.core.application.tournament.TournamentState;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
@Entity("tournaments")
public class TournamentEntity implements Identifiable {

	@Id
	private ObjectId id;

	@Reference
	private PlayerEntity creator;

	@Property
	private RestorableTournamentSettings settings;

	@Reference
	private Set<PlayerEntity> players;

	private List<GroupEntity> groups;

	private PlayOffEntity playOff;

	private TournamentState state;

	private TournamentEntity() {
	}

	public TournamentEntity(String id) {
		this.id = new ObjectId(id);
	}

	public TournamentEntity(PlayerEntity creator, RestorableTournamentSettings settings, Set<PlayerEntity> players,
							List<GroupEntity> groups, PlayOffEntity playOff, TournamentState state) {
		this.creator = creator;
		this.settings = validateSettings(settings);
		this.players = players;
		this.groups = groups;
		this.playOff = playOff;
		this.state = state;
	}

	public void setCreator(PlayerEntity creator) {
		this.creator = creator;
	}

	public void setSettings(RestorableTournamentSettings settings) {
		this.settings = validateSettings(settings);
	}

	public void setPlayers(Set<PlayerEntity> players) {
		this.players = players;
	}

	public void setGroups(List<GroupEntity> groups) {
		this.groups = groups;
	}

	public void setPlayOff(PlayOffEntity playOff) {
		this.playOff = playOff;
	}

	public void setState(TournamentState state) {
		this.state = state;
	}

	public String getId() {
		return id.toHexString();
	}

	public PlayerEntity getCreator() {
		return creator;
	}

	public RestorableTournamentSettings getSettings() {
		return settings;
	}

	public Set<PlayerEntity> getPlayers() {
		return Objects.requireNonNullElse(players, Collections.emptySet());
	}

	public List<GroupEntity> getGroups() {
		return groups;
	}

	public PlayOffEntity getPlayOff() {
		return playOff;
	}

	public TournamentState getState() {
		return state;
	}

	private static RestorableTournamentSettings validateSettings(RestorableTournamentSettings settings) {
		GameSettings baseGameSettings = settings.getGroupSettings().getBaseGameSettings();
		if (!(baseGameSettings instanceof RestorableGameSettings)) {
			throw new IllegalStateException();
		}

		baseGameSettings = settings.getPlayOffSettings().getBaseGameSettings();
		if (!(baseGameSettings instanceof RestorableGameSettings)) {
			throw new IllegalStateException();
		}

		return settings;
	}
}
