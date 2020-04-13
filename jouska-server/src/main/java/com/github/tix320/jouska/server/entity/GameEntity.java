package com.github.tix320.jouska.server.entity;

import java.util.List;

import com.github.tix320.jouska.core.application.game.GameChange;
import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

@Entity("games")
public class GameEntity {

	@Id
	private ObjectId id;

	@Reference
	private PlayerEntity creator;

	@BsonProperty(useDiscriminator = true)
	private GameSettings settings;

	private GameState state;

	@Reference
	private List<PlayerEntity> players;

	private List<InGamePlayer> gamePlayers;

	private List<GameChange> changes;

	@Embedded
	GameStatisticsSubEntity statistics;

	private GameEntity() {
	}

	public GameEntity(PlayerEntity creator, GameSettings settings, GameState state, List<PlayerEntity> players,
					  List<InGamePlayer> gamePlayers, List<GameChange> changes, GameStatisticsSubEntity statistics) {
		this.creator = creator;
		this.settings = settings;
		this.state = state;
		this.players = players;
		this.gamePlayers = gamePlayers;
		this.changes = changes;
		this.statistics = statistics;
	}

	public String getId() {
		return id.toHexString();
	}

	public PlayerEntity getCreator() {
		return creator;
	}

	public GameSettings getSettings() {
		return settings;
	}

	public GameState getState() {
		return state;
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

	public GameStatisticsSubEntity getStatistics() {
		return statistics;
	}
}
