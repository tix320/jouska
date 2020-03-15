package com.github.tix320.jouska.bot;

import java.util.List;

import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("game")
public interface BotGameService {

	@Origin("connect")
	MonoObservable<GameConnectionAnswer> connect(long gameId);

	@Origin("info")
	MonoObservable<List<GameView>> getGames();
}
