package com.github.tix320.jouska.server.infrastructure.origin;

import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Origin("auth")
public interface AuthenticationOrigin {

	@Origin("logout")
	void logout(@ClientID long clientId);
}
