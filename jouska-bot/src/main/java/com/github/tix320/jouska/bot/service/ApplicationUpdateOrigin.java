package com.github.tix320.jouska.bot.service;

import com.github.tix320.jouska.core.infrastructure.OS;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationUpdateOrigin {

	@Origin
	MonoObservable<String> getLatestVersion();

	@Origin
	MonoObservable<Transfer> downloadBot(OS os);
}
