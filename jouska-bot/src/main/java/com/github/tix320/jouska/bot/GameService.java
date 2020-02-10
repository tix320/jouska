package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("game")
public interface GameService {

	@Origin("connect")
	MonoObservable<GameConnectionAnswer> connect(long gameId);
}
