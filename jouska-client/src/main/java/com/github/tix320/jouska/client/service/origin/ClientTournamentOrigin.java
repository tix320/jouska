package com.github.tix320.jouska.client.service.origin;

import java.util.List;

import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.skimp.api.object.None;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Response;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("tournament")
public interface ClientTournamentOrigin {

	@Origin("list")
	@Subscription
	Observable<List<TournamentView>> getTournaments();

	@Origin
	MonoObservable<TournamentStructure> getTournamentStructure(String tournamentId);

	@Origin("create")
	MonoObservable<Response<None>> create(CreateTournamentCommand createTournamentCommand);

	@Origin("join")
	MonoObservable<Confirmation> join(String tournamentId);

	@Origin
	MonoObservable<None> startTournament(String tournamentId);
}
