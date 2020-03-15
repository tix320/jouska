package com.github.tix320.jouska.client.service.origin;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("player")
public interface PlayerService {

	@Origin("me")
	MonoObservable<Player> me();
}
