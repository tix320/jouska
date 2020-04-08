package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.tournament.PlayOff;
import com.github.tix320.jouska.core.application.tournament.Tournament;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupPlayerView;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupView;
import com.github.tix320.jouska.core.dto.TournamentStructure.PlayOffView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.infrastructure.application.TournamentManager;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.Role;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
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
								tournamentInfo.getRegisteredPlayers().size(),
								tournamentInfo.getTournamentSettings().getPlayersCount(), tournamentInfo.getCreator(),
								tournamentInfo.getTournament().isPresent()))
						.collect(Collectors.toList()));
	}

	@Endpoint
	public TournamentStructure getTournamentStructure(long tournamentId, @CallerUser Player player) {
		Tournament tournament = TournamentManager.getTournament(tournamentId);
		AtomicInteger groupIndex = new AtomicInteger(1);
		List<GroupView> groups = tournament.getGroups()
				.stream()
				.map(group -> new GroupView("Group " + groupIndex.getAndIncrement(), group.getPlayers()
						.stream()
						.map(groupPlayer -> new GroupPlayerView(groupPlayer.getPlayer(), groupPlayer.getGroupPoints(),
								groupPlayer.getTotalGamesPoints()))
						.collect(Collectors.toList())))
				.collect(Collectors.toList());

		PlayOffView playOffView;
		PlayOff playOff = Try.supply(() -> tournament.playOff().get(Duration.ofSeconds(0)))
				.recover(TimeoutException.class, e -> null)
				.forceGet();

		if (playOff == null) {
			playOffView = null;
		}
		else {
			List<List<PlayOffGameView>> playOffGamesViews = playOff.getGamesStructure()
					.stream()
					.map(tour -> tour.stream()
							.map(playOffGame -> new PlayOffGameView(playOffGame.getFirstPlayer(),
									playOffGame.getSecondPlayer(), playOffGame.getWinnerNumber()))
							.collect(Collectors.toList()))
					.collect(Collectors.toList());

			playOffView = new PlayOffView(playOff.getPlayers().size(), playOffGamesViews,
					playOff.getWinner().orElse(null));
		}

		return new TournamentStructure(tournamentId, groups, playOffView);
	}

	@Endpoint("create")
	@Role(RoleName.ADMIN)
	public long create(CreateTournamentCommand createTournamentCommand, @CallerUser Player player) {
		return TournamentManager.createNewTournament(createTournamentCommand, player);
	}

	@Endpoint("join")
	public TournamentJoinAnswer join(long tournamentId, @CallerUser Player player) {
		return TournamentManager.joinTournament(tournamentId, player);
	}

	@Endpoint
	@Role(RoleName.ADMIN)
	public void startTournament(long tournamentId, @CallerUser Player player) {
		TournamentManager.startTournament(tournamentId);
	}
}
