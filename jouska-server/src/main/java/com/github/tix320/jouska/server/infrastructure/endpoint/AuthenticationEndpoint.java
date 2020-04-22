package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.Set;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.RegistrationAnswer;
import com.github.tix320.jouska.core.dto.RegistrationCommand;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;
import com.mongodb.DuplicateKeyException;

@Endpoint("auth")
public class AuthenticationEndpoint {

	private final PlayerService playerService;

	public AuthenticationEndpoint() {
		playerService = new PlayerService();
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
		PlayerEntity playerEntity = new PlayerEntity(registrationCommand.getNickname(),
				registrationCommand.getPassword(), RoleName.PLAYER);
		try {
			DataSource.getInstance().save(playerEntity);
			return RegistrationAnswer.SUCCESS;
		}
		catch (DuplicateKeyException e) {
			return RegistrationAnswer.NICKNAME_ALREADY_EXISTS;
		}
	}

	@Endpoint("connected-players")
	@Subscription
	public Observable<Set<Player>> connectPlayers() {
		return playerService.getConnectedPlayers();
	}
}
