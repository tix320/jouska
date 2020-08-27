package com.github.tix320.jouska.client.app.inject;

import com.github.tix320.jouska.client.service.endpoint.AuthenticationEndpoint;
import com.github.tix320.jouska.client.service.endpoint.ClientGameEndpoint;
import com.github.tix320.jouska.client.service.endpoint.ClientTournamentEndpoint;
import com.github.tix320.ravel.api.Singleton;

public class EndpointModule {

	@Singleton
	public AuthenticationEndpoint applicationUpdateEndpoint() {
		return new AuthenticationEndpoint();
	}

	@Singleton
	public ClientGameEndpoint clientGameEndpoint() {
		return new ClientGameEndpoint();
	}

	@Singleton
	public ClientTournamentEndpoint clientTournamentEndpoint() {
		return new ClientTournamentEndpoint();
	}
}
