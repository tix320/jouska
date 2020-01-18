package com.gitlab.tixtix320.jouska.server.service;

import com.gitlab.tixtix320.jouska.core.dto.StartGameCommand;
import com.gitlab.tixtix320.jouska.core.dto.WatchGameCommand;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.util.None;
import com.gitlab.tixtix320.sonder.api.common.rpc.Origin;
import com.gitlab.tixtix320.sonder.api.common.rpc.extra.ClientID;

@Origin("game")
public interface GameService {

	@Origin("start")
	Observable<None> startGame(StartGameCommand startGameCommand, @ClientID long clientId);

	@Origin("watch")
	Observable<None> watchGame(WatchGameCommand watchGameCommand, @ClientID long clientId);
}
