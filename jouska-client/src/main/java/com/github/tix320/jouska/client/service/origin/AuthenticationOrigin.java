package com.github.tix320.jouska.client.service.origin;

import java.util.Set;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.RegistrationAnswer;
import com.github.tix320.jouska.core.dto.RegistrationCommand;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.skimp.api.object.None;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("auth")
public interface AuthenticationOrigin {

	@Origin("login")
	MonoObservable<LoginAnswer> login(Credentials credentials);

	@Origin("forceLogin")
	MonoObservable<LoginAnswer> forceLogin(Credentials credentials);

	@Origin("logout")
	MonoObservable<None> logout();

	@Origin("register")
	MonoObservable<RegistrationAnswer> register(RegistrationCommand registrationCommand);

	@Origin("connected-players")
	@Subscription
	Observable<Set<Player>> connectPlayers();
}
