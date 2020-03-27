package com.github.tix320.jouska.server.entity;

import java.util.List;

import com.github.tix320.jouska.core.application.game.GameChange;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import dev.morphia.annotations.Embedded;
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

	private List<InGamePlayer> gamePlayers;

	private List<GameChange> changes;

	@Embedded
	GameStatisticsSubEntity gameStatistics;

	private GameEntity() {
	}

	public GameEntity(List<PlayerEntity> players, List<InGamePlayer> gamePlayers, List<GameChange> changes,
					  GameStatisticsSubEntity gameStatistics) {
		this.players = players;
		this.gamePlayers = gamePlayers;
		this.changes = changes;
		this.gameStatistics = gameStatistics;
	}

	public ObjectId getId() {
		return id;
	}

	public List<PlayerEntity> getPlayers() {
		return players;
	}

	public List<InGamePlayer> getGamePlayers() {
		return gamePlayers;
	}

	public List<GameChange> getChanges() {
		return changes;
	}

	public GameStatisticsSubEntity getGameStatistics() {
		return gameStatistics;
	}
}
