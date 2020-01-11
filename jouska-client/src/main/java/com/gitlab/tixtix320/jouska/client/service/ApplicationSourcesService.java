package com.gitlab.tixtix320.jouska.client.service;

import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.sonder.api.common.communication.Transfer;
import com.gitlab.tixtix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationSourcesService {

	@Origin("latest-version")
	Observable<String> getLatestVersion();

	@Origin("latest-zip")
	Observable<Transfer> getApplicationLatestSourcesZip();
}
