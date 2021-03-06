package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.application.TournamentManager;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.origin.AuthenticationOrigin;
import com.github.tix320.jouska.server.infrastructure.origin.ServerGameManagementOrigin;
import com.github.tix320.jouska.server.infrastructure.origin.ServerTournamentOrigin;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.ravel.api.module.UseModules;
import com.github.tix320.ravel.api.scope.Singleton;

@UseModules(classes = DaoModule.class)
public class ServiceModule {

	@Singleton
	public PlayerService playerService(PlayerDao playerDao, AuthenticationOrigin authenticationOrigin,
									   ClientPlayerMappingResolver clientPlayerMappingResolver) {
		return new PlayerService(playerDao, authenticationOrigin, clientPlayerMappingResolver);
	}

	@Singleton
	public GameManager gameManager(ServerGameManagementOrigin gameManagementOrigin, GameDao gameDao,
								   ClientPlayerMappingResolver clientPlayerMappingResolver) {
		return new GameManager(gameManagementOrigin, gameDao, clientPlayerMappingResolver);
	}

	@Singleton
	public TournamentManager tournamentManager(ServerTournamentOrigin tournamentOrigin,
											   ClientPlayerMappingResolver clientPlayerMappingResolver) {
		return new TournamentManager(tournamentOrigin, clientPlayerMappingResolver);
	}

	@Singleton
	public ClientPlayerMappingResolver clientPlayerMappingResolver() {
		return new ClientPlayerMappingResolver();
	}
}
