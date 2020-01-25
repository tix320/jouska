package com.github.tix320.jouska.client.service;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationUpdateService {

	@Origin("check-update")
	MonoObservable<String> checkUpdate(String version, String os);

	@Origin("windows-latest")
	MonoObservable<Transfer> downloadWindowsLatest();

	@Origin("linux-latest")
	MonoObservable<Transfer> downloadLinuxLatest();

	@Origin("mac-latest")
	MonoObservable<Transfer> downloadMacLatest();
}
