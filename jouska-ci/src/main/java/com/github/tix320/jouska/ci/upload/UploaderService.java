package com.github.tix320.jouska.ci.upload;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface UploaderService {

	@Origin("upload-windows")
	Observable<None> uploadWindows(Transfer transfer);

	@Origin("upload-unix")
	Observable<None> uploadUnix(Transfer transfer);

}
