package com.github.tix320.jouska.client.service.origin;

import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.jouska.core.dto.RegistrationAnswer;
import com.github.tix320.jouska.core.dto.RegistrationCommand;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("auth")
public interface AuthenticationOrigin {

	@Origin("login")
	MonoObservable<LoginAnswer> login(LoginCommand loginCommand);

	@Origin("forceLogin")
	MonoObservable<LoginAnswer> forceLogin(LoginCommand loginCommand);

	@Origin("logout")
	MonoObservable<None> logout();

	@Origin("register")
	MonoObservable<RegistrationAnswer> register(RegistrationCommand registrationCommand);
}
