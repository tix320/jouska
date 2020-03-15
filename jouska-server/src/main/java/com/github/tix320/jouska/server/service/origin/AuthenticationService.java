package com.github.tix320.jouska.server.service.origin;

import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Origin("auth")
public interface AuthenticationService {

	@Origin("logout")
	void logout(@ClientID long clientId);
}
