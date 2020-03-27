package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.tournament.TournamentChange;
import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.infrastructure.application.TournamentManager;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.Role;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Endpoint("tournament")
public class ServerTournamentEndpoint {

	@Endpoint("list")
	@Subscription
	public Observable<List<TournamentView>> tournaments(@CallerUser Player player) {
		return TournamentManager.tournaments()
				.map(tournamentInfos -> tournamentInfos.stream()
						.map(tournamentInfo -> new TournamentView(tournamentInfo.getId(),
								tournamentInfo.getTournamentSettings().getName(),
								tournamentInfo.getTournamentSettings().getPlayersCount()))
						.collect(Collectors.toList()));
	}

	@Endpoint("tournamentChanges")
	@Subscription
	public Observable<TournamentChange> tournamentChanges(long tournamentId, @CallerUser Player player) {
		return TournamentManager.getTournament(tournamentId).changes();
	}

	@Endpoint("create")
	@Role(RoleName.ADMIN)
	public long create(CreateTournamentCommand createTournamentCommand, @CallerUser Player player) {
		return TournamentManager.createNewTournament(createTournamentCommand);
	}

	@Endpoint("join")
	public void join(long tournamentId) {
		TournamentManager.joinTournament(tournamentId);
	}
}