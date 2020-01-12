package com.gitlab.tixtix320.jouska.ci.upload;

import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.util.None;
import com.gitlab.tixtix320.sonder.api.common.communication.Transfer;
import com.gitlab.tixtix320.sonder.api.common.rpc.Origin;

@Origin("application")
public interface UploaderService {

	@Origin("upload")
	Observable<None> upload(Transfer transfer);
}
