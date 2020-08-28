package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.app.DatastoreHolder;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.dao.TournamentDao;
import com.github.tix320.ravel.api.Singleton;

public class DaoModule {

	@Singleton
	public DatastoreHolder datastoreHolder() {
		return new DatastoreHolder();
	}

	@Singleton
	public GameDao gameDao(DatastoreHolder datastoreHolder) {
		return new GameDao(datastoreHolder);
	}

	@Singleton
	public PlayerDao playerDao(DatastoreHolder datastoreHolder) {
		return new PlayerDao(datastoreHolder);
	}

	@Singleton
	public TournamentDao tournamentDao(DatastoreHolder datastoreHolder) {
		return new TournamentDao(datastoreHolder);
	}
}
