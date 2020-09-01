package com.github.tix320.jouska.server.infrastructure.dao;

import com.github.tix320.jouska.server.app.DatastoreProvider;
import com.github.tix320.jouska.server.infrastructure.entity.TournamentEntity;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class TournamentDao extends BaseDao<TournamentEntity> {

	public TournamentDao(DatastoreProvider datastoreProvider) {
		super(datastoreProvider);
	}

	@Override
	protected Class<TournamentEntity> getEntityClass() {
		return TournamentEntity.class;
	}
}
