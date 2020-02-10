package com.github.tix320.jouska.server.service;

import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Origin("game")
public interface ServerGameService {

	@Origin("start")
	MonoObservable<None> startGame(StartGameCommand startGameCommand, @ClientID long clientId);

	@Origin("watch")
	MonoObservable<None> watchGame(WatchGameCommand watchGameCommand, @ClientID long clientId);
}
