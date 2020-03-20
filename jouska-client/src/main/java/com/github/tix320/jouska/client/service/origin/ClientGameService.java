package com.github.tix320.jouska.client.service.origin;

import java.util.List;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscribe;

@Origin("game")
public interface ClientGameService {

	@Origin("info")
	@Subscribe
	Observable<List<GameView>> games();

	@Origin("connect")
	MonoObservable<GameConnectionAnswer> connect(long gameId);

	@Origin("create")
	MonoObservable<Long> create(CreateGameCommand createGameCommand);

	@Origin("watch")
	void watch(long gameId);
}
