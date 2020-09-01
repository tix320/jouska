package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.app.DatastoreProvider;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.dao.TournamentDao;
import com.github.tix320.ravel.api.scope.Singleton;

public class DaoModule {

	@Singleton
	public DatastoreProvider datastoreHolder() {
		return new DatastoreProvider();
	}

	@Singleton
	public GameDao gameDao(DatastoreProvider datastoreProvider) {
		return new GameDao(datastoreProvider);
	}

	@Singleton
	public PlayerDao playerDao(DatastoreProvider datastoreProvider) {
		return new PlayerDao(datastoreProvider);
	}

	@Singleton
	public TournamentDao tournamentDao(DatastoreProvider datastoreProvider) {
		return new TournamentDao(datastoreProvider);
	}
}
