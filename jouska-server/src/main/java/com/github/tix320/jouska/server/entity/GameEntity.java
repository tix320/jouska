package com.github.tix320.jouska.server.entity;

import java.util.List;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.types.ObjectId;

@Entity("games")
public class GameEntity {

	@Id
	private ObjectId id;

	@Reference
	private List<PlayerEntity> players;

	private GameEntity() {
	}

	public GameEntity(List<PlayerEntity> players) {
		this.players = players;
	}

	public ObjectId getId() {
		return id;
	}

	public List<PlayerEntity> getPlayers() {
		return players;
	}
}
