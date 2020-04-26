package com.github.tix320.jouska.bot.service;

import java.util.List;

import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("tournament")
public interface BotTournamentOrigin {

	@Origin("list")
	@Subscription
	Observable<List<TournamentView>> getTournaments();

	@Origin
	MonoObservable<Confirmation> join(String tournamentId);
}
