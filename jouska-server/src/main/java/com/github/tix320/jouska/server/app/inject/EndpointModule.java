package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.application.TournamentManager;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.dao.TournamentDao;
import com.github.tix320.jouska.server.infrastructure.endpoint.*;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.UserExtraArgInjector;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.ravel.api.module.UseModules;
import com.github.tix320.ravel.api.scope.Singleton;

@UseModules(classes = {ServiceModule.class, DaoModule.class}, names = "origins")
public class EndpointModule {

	@Singleton
	public ApplicationUpdateEndpoint applicationUpdateEndpoint() {
		return new ApplicationUpdateEndpoint();
	}

	@Singleton
	public AuthenticationEndpoint authenticationEndpoint(PlayerDao playerDao, PlayerService playerService) {
		return new AuthenticationEndpoint(playerService, playerDao);
	}

	@Singleton
	public ServerGameEndpoint serverGameEndpoint(GameDao gameDao, GameManager gameManager) {
		return new ServerGameEndpoint(gameDao, gameManager);
	}

	@Singleton
	public ServerGameManagementEndpoint serverGameManagementEndpoint(GameDao gameDao, TournamentDao tournamentDao,
																	 GameManager gameManager) {
		return new ServerGameManagementEndpoint(gameDao, tournamentDao, gameManager);
	}

	@Singleton
	public ServerPlayerEndpoint serverPlayerEndpoint(PlayerDao playerDao) {
		return new ServerPlayerEndpoint(playerDao);
	}

	@Singleton
	public ServerTournamentEndpoint serverTournamentEndpoint(TournamentManager tournamentManager) {
		return new ServerTournamentEndpoint(tournamentManager);
	}

	@Singleton
	public UserExtraArgInjector userExtraArgExtractor(PlayerService playerService,
													  ClientPlayerMappingResolver clientPlayerMappingResolver) {
		return new UserExtraArgInjector(playerService, clientPlayerMappingResolver);
	}
}
