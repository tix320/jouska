package com.github.tix320.jouska.server.infrastructure.origin;

import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Origin("game")
public interface ServerGameOrigin {

	@Origin
	MonoObservable<None> notifyGameStarted(GamePlayDto gamePlayDto, @ClientID long clientId);
}
