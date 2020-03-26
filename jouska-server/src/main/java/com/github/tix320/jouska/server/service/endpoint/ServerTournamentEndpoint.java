package com.github.tix320.jouska.server.service.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.service.application.TournamentManager;
import com.github.tix320.jouska.server.service.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.service.endpoint.auth.Role;
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

	@Endpoint("structure")
	public TournamentStructure getStructure(long tournamentId, @CallerUser Player player) {
		return new TournamentStructure(List.of());//TournamentManager.getTournament(tournamentId);
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
