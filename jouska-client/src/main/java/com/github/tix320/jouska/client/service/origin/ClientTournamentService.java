package com.github.tix320.jouska.client.service.origin;

import java.util.List;

import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("tournament")
public interface ClientTournamentService {

	@Origin("list")
	MonoObservable<List<TournamentView>> getTournaments();

	@Origin("structure")
	MonoObservable<TournamentStructure> getStructure(long tournamentId);

	@Origin("create")
	MonoObservable<None> create(CreateTournamentCommand createTournamentCommand);
}
