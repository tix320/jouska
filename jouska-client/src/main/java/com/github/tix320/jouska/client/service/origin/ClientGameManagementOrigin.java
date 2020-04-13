package com.github.tix320.jouska.client.service.origin;

import java.util.List;

import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.dto.GameWatchDto;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("game")
public interface ClientGameManagementOrigin {

	@Origin("info")
	@Subscription
	Observable<List<GameView>> games();

	@Origin("create")
	MonoObservable<String> create(CreateGameCommand createGameCommand);

	@Origin("start")
	void startGame(String gameId);

	@Origin
	MonoObservable<GameConnectionAnswer> join(String gameId);

	@Origin
	void leave(String gameId);

	@Origin("watch")
	MonoObservable<GameWatchDto> watch(String gameId);
}
