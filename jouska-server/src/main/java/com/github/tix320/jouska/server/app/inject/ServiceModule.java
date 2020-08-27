package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.application.TournamentManager;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.origin.AuthenticationOrigin;
import com.github.tix320.jouska.server.infrastructure.origin.ServerGameManagementOrigin;
import com.github.tix320.jouska.server.infrastructure.origin.ServerTournamentOrigin;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.ravel.api.Singleton;
import com.github.tix320.ravel.api.UseModules;

@UseModules(classes = DaoModule.class)
public class ServiceModule {

	@Singleton
	public PlayerService playerService(PlayerDao playerDao, AuthenticationOrigin authenticationOrigin) {
		return new PlayerService(playerDao, authenticationOrigin);
	}

	@Singleton
	public GameManager gameManager(ServerGameManagementOrigin gameManagementOrigin, GameDao gameDao) {
		return new GameManager(gameManagementOrigin, gameDao);
	}

	@Singleton
	public TournamentManager tournamentManager(ServerTournamentOrigin tournamentOrigin) {
		return new TournamentManager(tournamentOrigin);
	}
}
