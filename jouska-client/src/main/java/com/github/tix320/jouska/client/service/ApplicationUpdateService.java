package com.github.tix320.jouska.client.service;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationUpdateService {

	@Origin("check-update")
	Observable<Boolean> checkUpdate(String version);

	@Origin("windows-latest")
	Observable<Transfer> downloadWindowsLatest();

	@Origin("linux-latest")
	Observable<Transfer> downloadLinuxLatest();

	@Origin("mac-latest")
	Observable<Transfer> downloadMacLatest();
}
