package com.github.tix320.jouska.server.infrastructure.application;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.tournament.ClassicTournament;
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
		return tournamentInfo.getTournament()
				.orElseThrow(
						() -> new IllegalArgumentException(String.format("Tournament %s not started", tournamentId)));
	}

	public static TournamentJoinAnswer joinTournament(long tournamentId, Player player) {
		AtomicReference<TournamentJoinAnswer> answer = new AtomicReference<>(TournamentJoinAnswer.REJECT);
		tournaments.computeIfPresent(tournamentId, (key, tournamentInfo) -> {
			int maxPlayersCount = tournamentInfo.getTournamentSettings().getPlayersCount();

			if (tournamentInfo.getTournament().isPresent()) {
				throw new IllegalStateException(String.format("Tournament %s already started", tournamentId));
			}

			if (tournamentInfo.getRegisteredPlayers().size() < maxPlayersCount) {
				if (player.equals(tournamentInfo.getCreator())) {
					tournamentInfo.getRegisteredPlayers().add(player);
					answer.set(TournamentJoinAnswer.ACCEPT);
				}
				else if (tournamentInfo.getRegisteredPlayers().contains(player)) {
					answer.set(TournamentJoinAnswer.ACCEPT);
				}
				else {
					Long creatorClientId = ClientPlayerMappingResolver.getClientIdByPlayer(
							tournamentInfo.getCreator().getId()).orElseThrow();

					TournamentJoinRequest request = new TournamentJoinRequest(
							new TournamentView(tournamentInfo.getId(), tournamentInfo.getTournamentSettings().getName(),
									tournamentInfo.getRegisteredPlayers().size(), maxPlayersCount,
									tournamentInfo.getCreator(), false), player);
					try {
						TournamentJoinAnswer requestAnswer = TOURNAMENT_ORIGIN.requestTournamentJoin(request,
								creatorClientId).get(Duration.ofSeconds(15));

						if (requestAnswer == TournamentJoinAnswer.ACCEPT) {
							tournamentInfo.getRegisteredPlayers().add(player);
						}

						answer.set(requestAnswer);
					}
					catch (TimeoutException e) {
						answer.set(TournamentJoinAnswer.REJECT);
					}
				}
			}
			else {
				answer.set(TournamentJoinAnswer.REJECT);
			}
			return tournamentInfo;
		});
		return answer.get();
	}

	public static void startTournament(long tournamentId) {
		tournaments.computeIfPresent(tournamentId, (id, tournamentInfo) -> {
			if (tournamentInfo.getTournament().isPresent()) {
				throw new IllegalStateException(String.format("Tournament %s already started", id));
			}

			ClassicTournament tournament = new ClassicTournament(tournamentInfo.getTournamentSettings(),
					tournamentInfo.getRegisteredPlayers());
			tournamentInfo.setTournament(tournament);

			List<GameRegistration> groupGames = tournament.getGroups()
					.stream()
					.flatMap(group -> group.getGames().stream())
					.map(gameWithSettings -> new GameRegistration(gameWithSettings, tournamentInfo.getCreator()))
					.collect(Collectors.toList());

			GameManager.registerGames(groupGames);

			tournament.playOff().subscribe(playOff -> playOff.createdGames().subscribe(gameWithSettings -> {
				GameRegistration gameRegistration = new GameRegistration(gameWithSettings, tournamentInfo.getCreator());
				GameManager.registerGames(List.of(gameRegistration));
			}));

			return tournamentInfo;
		});
	}

	private static void failIfTournamentNull(long tournamentId, TournamentInfo tournamentInfo) {
		if (tournamentInfo == null) {
			throw new IllegalArgumentException(String.format("Tournament `%s` does not exists", tournamentId));
		}
	}
}
