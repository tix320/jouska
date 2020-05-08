package com.github.tix320.jouska.ci.upload;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface UploaderService {

	@Origin
	MonoObservable<None> uploadWindowsClient(Transfer transfer);

	@Origin
	MonoObservable<None> uploadLinuxClient(Transfer transfer);

	@Origin
	MonoObservable<None> uploadMacClient(Transfer transfer);
}
