package com.github.tix320.jouska.server.infrastructure.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.entity.GameEntity;
import com.github.tix320.jouska.server.entity.GameStatisticsSubEntity;
import com.github.tix320.jouska.server.entity.PlayerEntity;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import org.bson.types.ObjectId;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class GameService {

	public static String saveGame(GameSettings gameSettings, String creatorId) {
		GameEntity gameEntity = new GameEntity(new PlayerEntity(new ObjectId(creatorId)), gameSettings,
				GameState.INITIAL, null, null, null, null);
		DataSource.getInstance().save(gameEntity);
		return gameEntity.getId();
	}

	public static void updateGame(String gameId, Game game) {
		List<InGamePlayer> players = game.getPlayers();
		GameStatisticsSubEntity gameStatistics = new GameStatisticsSubEntity(
				adaptStatistics(game.getStatistics().summaryPoints()));

		List<PlayerEntity> playerEntities = players.stream()
				.map(inGamePlayer -> new PlayerEntity(new ObjectId(inGamePlayer.getRealPlayer().getId())))
				.collect(Collectors.toList());

		List<GameChange> changes = game.changes().list();

		Datastore instance = DataSource.getInstance();
		Query<GameEntity> findQuery = instance.createQuery(GameEntity.class).field("_id").equal(new ObjectId(gameId));
		UpdateOperations<GameEntity> updateOperations = instance.createUpdateOperations(GameEntity.class)
				.set("state", game.getState())
				.set("players", playerEntities)
				.set("gamePlayers", game.getPlayers())
				.set("changes", changes)
				.set("statistics", gameStatistics);
		instance.update(findQuery, updateOperations);
	}

	public static List<GameEntity> getGames(List<String> fieldsToFetch, Map<String, Object> filters) {
		Query<GameEntity> query = DataSource.getInstance().find(GameEntity.class);
		query.project("_id", true);
		for (String field : fieldsToFetch) {
			query.project(field, true);
		}
		filters.forEach((field, value) -> query.field(field).equal(value));

		return query.find().toList();
	}

	public static Optional<GameEntity> getGame(String gameId) {
		return Optional.ofNullable(
				DataSource.getInstance().find(GameEntity.class).field("_id").equal(new ObjectId(gameId)).first());
	}

	public static Optional<GameEntity> getGame(String gameId, List<String> fieldsToFetch) {
		Query<GameEntity> query = DataSource.getInstance().find(GameEntity.class);
		query.project("_id", true);
		for (String field : fieldsToFetch) {
			query.project(field, true);
		}
		query.field("_id").equal(new ObjectId(gameId));

		return Optional.ofNullable(query.first());
	}

	private static Map<PlayerColor, Integer> adaptStatistics(Map<InGamePlayer, Integer> statistics) {
		return statistics.entrySet().stream().collect(toMap(entry -> entry.getKey().getColor(), Entry::getValue));
	}
}
