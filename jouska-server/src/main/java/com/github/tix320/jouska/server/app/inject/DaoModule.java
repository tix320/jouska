package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.dao.TournamentDao;
import com.github.tix320.ravel.api.Singleton;

public class DaoModule {

	@Singleton
	public GameDao gameDao() {
		return new GameDao();
	}

	@Singleton
	public PlayerDao playerDao() {
		return new PlayerDao();
	}

	@Singleton
	public TournamentDao tournamentDao() {
		return new TournamentDao();
	}
}
