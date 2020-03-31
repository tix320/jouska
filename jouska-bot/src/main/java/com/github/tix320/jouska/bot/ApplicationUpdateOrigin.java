package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.infrastructure.OS;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationUpdateOrigin {

	@Origin
	MonoObservable<String> checkUpdate(String version, OS os);

	@Origin
	MonoObservable<Transfer> downloadBot(OS os);
}
