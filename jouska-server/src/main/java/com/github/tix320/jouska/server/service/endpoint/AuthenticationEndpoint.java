package com.github.tix320.jouska.server.service.endpoint;

import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.entity.PlayerEntity;
import com.github.tix320.jouska.server.service.application.PlayerService;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;
import com.mongodb.DuplicateKeyException;

@Endpoint("auth")
public class AuthenticationEndpoint {

	@Endpoint("login")
	public LoginAnswer login(LoginCommand loginCommand, @ClientID long clientId) {
		return PlayerService.login(clientId, loginCommand);
	}

	@Endpoint("forceLogin")
	public LoginAnswer forceLogin(LoginCommand loginCommand, @ClientID long clientId) {
		return PlayerService.forceLogin(clientId, loginCommand);
	}

	@Endpoint("logout")
	public void logout(@ClientID long clientId) {
		PlayerService.logout(clientId);
	}

	@Endpoint("register")
	public RegistrationAnswer register(RegistrationCommand registrationCommand, @ClientID long clientId) {
		PlayerEntity playerEntity = new PlayerEntity(registrationCommand.getNickname(),
				registrationCommand.getPassword(), RoleName.PLAYER);
		try {
			DataSource.INSTANCE.save(playerEntity);
			return RegistrationAnswer.SUCCESS;
		}
		catch (DuplicateKeyException e) {
			return RegistrationAnswer.NICKNAME_ALREADY_EXISTS;
		}
	}
}
