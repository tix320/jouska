package com.github.tix320.jouska.server.infrastructure.dao;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.tix320.jouska.server.app.DatastoreHolder;
import com.github.tix320.jouska.server.infrastructure.entity.GameEntity;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class GameDao extends BaseDao<GameEntity> {

	private static final Map<String, Function<GameEntity, ?>> FIELD_RETRIEVERS = Map.of("creator",
			GameEntity::getCreator, "settings", GameEntity::getSettings, "state", GameEntity::getState, "players",
			GameEntity::getPlayers, "changes", GameEntity::getChanges, "statistics", GameEntity::getStatistics);

	public GameDao(DatastoreHolder datastoreHolder) {
		super(datastoreHolder);
	}

	public void update(GameEntity gameEntity, List<String> fieldsToUpdate) {
		Map<String, Function<GameEntity, ?>> fieldsWithRetrievers = fieldsToUpdate.stream()
				.collect(toMap(s -> s, FIELD_RETRIEVERS::get));

		update(gameEntity, fieldsWithRetrievers);
	}

	@Override
	protected Class<GameEntity> getEntityClass() {
		return GameEntity.class;
	}
}
