package com.github.tix320.jouska.client.service;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationSourcesService {

	@Origin("latest-version")
	Observable<String> getLatestVersion();

	@Origin("latest-zip")
	Observable<Transfer> getApplicationLatestSourcesZip();
}
