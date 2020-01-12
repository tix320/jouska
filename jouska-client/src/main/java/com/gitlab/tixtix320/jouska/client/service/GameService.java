package com.gitlab.tixtix320.jouska.client.service;

import java.util.List;

import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.util.None;
import com.gitlab.tixtix320.sonder.api.common.rpc.Origin;

@Origin("game")
public interface GameService {

	@Origin("info")
	Observable<List<GameInfo>> getGames();

	@Origin("connect")
	Observable<None> connect(long gameId);

	@Origin("create")
	Observable<Long> create(GameInfo gameInfo);
}
