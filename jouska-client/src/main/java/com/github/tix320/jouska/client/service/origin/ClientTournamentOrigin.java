package com.github.tix320.jouska.client.service.origin;

import java.util.List;

import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("tournament")
public interface ClientTournamentOrigin {

	@Origin("list")
	@Subscription
	Observable<List<TournamentView>> getTournaments();

	@Origin("structure")
	MonoObservable<TournamentStructure> getStructure(long tournamentId);

	@Origin("create")
	MonoObservable<None> create(CreateTournamentCommand createTournamentCommand);

	@Origin("join")
	MonoObservable<None> join(long tournamentId);
}