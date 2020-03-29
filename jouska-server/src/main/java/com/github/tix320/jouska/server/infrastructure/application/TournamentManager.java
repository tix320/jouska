package com.github.tix320.jouska.server.infrastructure.application;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tix320.jouska.core.application.tournament.Tournament;
import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.TournamentJoinAnswer;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.kiwi.api.reactive.property.MapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.IDGenerator;

import static com.github.tix320.jouska.server.app.Services.TOURNAMENT_ORIGIN;

public class TournamentManager {

	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final MapProperty<Long, TournamentInfo> tournaments = Property.forMap(new ConcurrentHashMap<>());

	public static long createNewTournament(CreateTournamentCommand createTournamentCommand, Player creator) {
		long tournamentId = ID_GENERATOR.next();
		tournaments.put(tournamentId,
				new TournamentInfo(tournamentId, creator, createTournamentCommand.getTournamentSettings()));
		return tournamentId;
	}

	public static Observable<Collection<TournamentInfo>> tournaments() {
		return tournaments.asObservable().map(Map::values);
	}

	public static Tournament getTournament(long tournamentId) {
		TournamentInfo tournamentInfo = tournaments.get(tournamentId);
		failIfTournamentNull(tournamentId, tournamentInfo);
		return tournamentInfo.getTournament();
	}

	public static synchronized TournamentJoinAnswer joinTournament(long tournamentId, Player player) {
		TournamentInfo tournamentInfo = tournaments.get(tournamentId);
		failIfTournamentNull(tournamentId, tournamentInfo);

		int maxPlayersCount = tournamentInfo.getTournamentSettings().getPlayersCount();

		if (tournamentInfo.getRegisteredPlayers().size() < maxPlayersCount) {
			Long creatorClientId = ClientPlayerMappingResolver.getClientIdByPlayer(tournamentInfo.getCreator().getId())
					.orElseThrow();

			TournamentJoinRequest request = new TournamentJoinRequest(
					new TournamentView(tournamentInfo.getId(), tournamentInfo.getTournamentSettings().getName(), -1),
					player);
			try {
				TournamentJoinAnswer requestAnswer = TOURNAMENT_ORIGIN.requestTournamentJoin(request, creatorClientId)
						.get(Duration.ofSeconds(15));

				if (requestAnswer == TournamentJoinAnswer.ACCEPT) {
					tournamentInfo.getRegisteredPlayers().add(player);
				}

				return requestAnswer;
			}
			catch (TimeoutException e) {
				return TournamentJoinAnswer.REJECT;
			}
		}
		else {
			return TournamentJoinAnswer.REJECT;
		}
	}

	private static void failIfTournamentNull(long tournamentId, TournamentInfo tournamentInfo) {
		if (tournamentInfo == null) {
			throw new IllegalArgumentException(String.format("Tournament `%s` does not exists", tournamentId));
		}
	}

}
