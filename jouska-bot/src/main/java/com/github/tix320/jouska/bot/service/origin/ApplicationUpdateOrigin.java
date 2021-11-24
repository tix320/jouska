package com.github.tix320.jouska.bot.service.origin;

import com.github.tix320.deft.api.OS;
import com.github.tix320.jouska.core.Version;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface ApplicationUpdateOrigin {

	@Origin
	MonoObservable<Version> getVersion();

	@Origin
	MonoObservable<Transfer> downloadBot(OS os);
}
