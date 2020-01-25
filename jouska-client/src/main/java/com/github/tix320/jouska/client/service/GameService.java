package com.github.tix320.jouska.client.service;

import java.util.List;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.model.GameInfo;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("game")
public interface GameService {

	@Origin("info")
	MonoObservable<List<GameInfo>> getGames();

	@Origin("connect")
	MonoObservable<GameConnectionAnswer> connect(long gameId);

	@Origin("create")
	MonoObservable<Long> create(CreateGameCommand createGameCommand);

	@Origin("watch")
	void watch(long gameId);
}
