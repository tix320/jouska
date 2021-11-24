package com.github.tix320.jouska.bot.service.origin;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("auth")
public interface AuthenticationService {

	@Origin("forceLogin")
	MonoObservable<LoginAnswer> forceLogin(Credentials credentials);
}
