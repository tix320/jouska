package com.github.tix320.jouska.server.infrastructure.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.server.app.DatastoreProvider;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import dev.morphia.query.experimental.filters.Filters;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayerDao extends BaseDao<PlayerEntity> {

	public PlayerDao(DatastoreProvider datastoreProvider) {
		super(datastoreProvider);
	}

	public Optional<PlayerEntity> findPlayerByCredentials(Credentials credentials) {
		return find(Filters.and(Filters.eq("nickname", credentials.getNickname()),
				Filters.eq("password", credentials.getPassword())));
	}

	public List<PlayerEntity> findPlayersByNickname(List<String> nicknames) {
		List<PlayerEntity> players = findAll(Filters.in("nickname", nicknames));

		Map<String, PlayerEntity> playerEntities = players.stream()
				.collect(toMap(PlayerEntity::getNickname, entity -> entity));

		return nicknames.stream().map(playerEntities::get).collect(Collectors.toList());
	}

	@Override
	protected Class<PlayerEntity> getEntityClass() {
		return PlayerEntity.class;
	}
}
