package com.github.tix320.jouska.server.service;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Origin("in-game")
public interface ServerInGameService {

	@Origin("turn")
	MonoObservable<None> turn(Point point, @ClientID long clientId);

	@Origin("leave")
	MonoObservable<None> leave(Player player, @ClientID long clientId);
}
