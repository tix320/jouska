package com.github.tix320.jouska.server.infrastructure.dao;

import com.github.tix320.jouska.server.app.DatastoreHolder;
import com.github.tix320.jouska.server.infrastructure.entity.TournamentEntity;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class TournamentDao extends BaseDao<TournamentEntity> {

	public TournamentDao(DatastoreHolder datastoreHolder) {
		super(datastoreHolder);
	}

	@Override
	protected Class<TournamentEntity> getEntityClass() {
		return TournamentEntity.class;
	}
}
