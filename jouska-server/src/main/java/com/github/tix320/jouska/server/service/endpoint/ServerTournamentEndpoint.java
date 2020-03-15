package com.github.tix320.jouska.server.service.endpoint;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.service.application.TournamentManager;
import com.github.tix320.jouska.server.service.endpoint.authentication.NeedAuthentication;
import com.github.tix320.jouska.server.service.endpoint.role.Role;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Endpoint("tournament")
public class ServerTournamentEndpoint {

	@Endpoint("list")
	@NeedAuthentication
	public List<TournamentView> getTournaments(@ClientID long clientId) {
		return TournamentManager.getTournaments()
				.stream()
				.map(tournamentInfo -> new TournamentView(tournamentInfo.getId(), tournamentInfo.getName(),
						tournamentInfo.getPlayersCount()))
				.collect(Collectors.toList());
	}

	@Endpoint("structure")
	@NeedAuthentication
	public TournamentStructure getStructure(long tournamentId, @ClientID long clientId) {
		return null;
		// return new TournamentStructure(List.of(new GroupView(Set.of(new Player(10, "aaa"))),
		// 		new GroupView(Set.of(new Player(20, "bbb"), new Player(30, "ccc")))));
	}

	@Endpoint("create")
	@NeedAuthentication
	@Role(RoleName.ADMIN)
	public long create(CreateTournamentCommand createTournamentCommand, @ClientID long clientId) {
		return TournamentManager.createNewTournament(createTournamentCommand);
	}
}
