package com.github.tix320.jouska.bot;

import java.util.List;

import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("game")
public interface BotGameManagementOrigin {

	@Origin
	MonoObservable<GameConnectionAnswer> join(long gameId);

	@Origin("info")
	@Subscription
	Observable<List<GameView>> games();
}
