package com.github.tix320.jouska.ci.upload;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("auth")
public interface AuthenticationService {

	@Origin("forceLogin")
	MonoObservable<LoginAnswer> forceLogin(Credentials credentials);
}
