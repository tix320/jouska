package com.github.tix320.jouska.server.infrastructure.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.infrastructure.dao.query.filter.Filter;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import dev.morphia.query.Query;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayerDao extends BaseDao<PlayerEntity> {

	public Optional<PlayerEntity> findPlayerByCredentials(Credentials credentials) {
		return
				find(Filter.and(Filter.equal("nickname", credentials.getNickname()),
				Filter.equal("password", credentials.getPassword())));
	}

	public List<PlayerEntity> findPlayersByNickname(List<String> nicknames) {
		Query<PlayerEntity> query = DataSource.getInstance().find(PlayerEntity.class);
		query.and(query.criteria("nickname").in(nicknames));

		Map<String, PlayerEntity> playerEntities = query.find()
				.toList()
				.stream()
				.collect(toMap(PlayerEntity::getNickname, entity -> entity));

		return nicknames.stream().map(playerEntities::get).collect(Collectors.toList());
	}

	@Override
	protected Class<PlayerEntity> getEntityClass() {
		return PlayerEntity.class;
	}
}