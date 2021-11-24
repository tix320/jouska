package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.RegistrationAnswer;
import com.github.tix320.jouska.core.dto.RegistrationCommand;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Role;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("auth")
public class AuthenticationEndpoint {

	private final PlayerService playerService;

	private final PlayerDao playerDao;

	public AuthenticationEndpoint(PlayerService playerService, PlayerDao playerDao) {
		this.playerService = playerService;
		this.playerDao = playerDao;
	}

	@Endpoint("login")
	public LoginAnswer login(Credentials credentials, @ClientID long clientId) {
		return playerService.login(clientId, credentials);
	}

	@Endpoint("forceLogin")
	public LoginAnswer forceLogin(Credentials credentials, @ClientID long clientId) {
		return playerService.forceLogin(clientId, credentials);
	}

	@Endpoint("logout")
	public void logout(@ClientID long clientId) {
		playerService.logout(clientId);
	}

	@Endpoint("register")
	public RegistrationAnswer register(RegistrationCommand registrationCommand, @ClientID long clientId) {
		if (playerDao.findPlayersByNickname(List.of(registrationCommand.getNickname())).get(0) != null) {
			return RegistrationAnswer.NICKNAME_ALREADY_EXISTS;
		}

		PlayerEntity playerEntity = new PlayerEntity(registrationCommand.getNickname(),
				registrationCommand.getPassword(), Role.PLAYER);
		playerDao.save(playerEntity);
		return RegistrationAnswer.SUCCESS;
	}

	@Endpoint("connected-players")
	@Subscription
	public Observable<Set<Player>> connectPlayers() {
		return playerService.getConnectedPlayers();
	}
}
