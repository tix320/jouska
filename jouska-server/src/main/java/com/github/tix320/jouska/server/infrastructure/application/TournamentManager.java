package com.github.tix320.jouska.server.infrastructure.application;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import com.github.tix320.jouska.core.application.game.GameAlreadyFullException;
import com.github.tix320.jouska.core.application.game.creation.RestorableTournamentSettings;
import com.github.tix320.jouska.core.application.game.creation.TournamentSettings;
import com.github.tix320.jouska.core.application.tournament.Tournament;
import com.github.tix320.jouska.core.application.tournament.TournamentIllegalStateException;
import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.application.dbo.DBTournament;
import com.github.tix320.jouska.server.infrastructure.application.dbo.DBTournamentSettings;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.util.collection.Tuple;

import static com.github.tix320.jouska.server.app.Services.TOURNAMENT_ORIGIN;

public class TournamentManager {

	private static final SinglePublisher<None> changesPublisher = new SinglePublisher<>(None.SELF);

	public static Observable<Collection<DBTournament>> tournaments() {
		return Observable.combineLatest(changesPublisher.asObservable(), DBTournament.all().asObservable())
				.map(Tuple::second)
				.map(Map::values);
	}

	public static String createTournament(RestorableTournamentSettings settings, Player creator) {
		DBTournamentSettings tournamentSettings = DBTournamentSettings.wrap(settings, creator);

		DBTournament dbTournament = DBTournament.createNew(tournamentSettings);
		return dbTournament.getId();
	}

	public static Confirmation joinTournament(String tournamentId, Player player) {
		DBTournament tournament = DBTournament.all().get(tournamentId);

		failIfTournamentNull(tournamentId, tournament);

		if (tournament.getPlayers().contains(player)) {
			return Confirmation.ACCEPT;
		}

		Player creator = tournament.getCreator();

		boolean canJoin = false;
		if (player.equals(creator)) {
			canJoin = true;
		}

		Long creatorClientId = ClientPlayerMappingResolver.getClientIdByPlayer(creator.getId()).orElseThrow();

		TournamentJoinRequest request = new TournamentJoinRequest(
				new TournamentView(tournament.getId(), tournament.getSettings().getName(),
						tournament.getPlayers().size(), tournament.getSettings().getMaxPlayersCount(), creator, false),
				player);
		try {
			Confirmation requestAnswer = TOURNAMENT_ORIGIN.requestTournamentJoin(request, creatorClientId)
					.get(Duration.ofSeconds(15));

			if (requestAnswer == Confirmation.ACCEPT) {
				canJoin = true;
			}
		}
		catch (TimeoutException e) {
			canJoin = false;
		}

		if (!canJoin) {
			return Confirmation.REJECT;
		}

		try {
			tournament.addPlayer(player);
			changesPublisher.publish(None.SELF);
			return Confirmation.ACCEPT;
		}
		catch (TournamentIllegalStateException | GameAlreadyFullException e) {
			return Confirmation.REJECT;
		}

	}

	public static Tournament getTournament(String tournamentId) {
		Tournament tournament = DBTournament.all().get(tournamentId);
		failIfTournamentNull(tournamentId, tournament);

		return tournament;
	}

	public static void startTournament(String tournamentId) {
		DBTournament tournament = DBTournament.all().get(tournamentId);
		failIfTournamentNull(tournamentId, tournament);

		tournament.start();

		changesPublisher.publish(None.SELF);

		tournament.completed().subscribe(TournamentManager::onTournamentComplete);
	}

	private static void onTournamentComplete(DBTournament tournament) {
		String tournamentId = tournament.getId();
		TournamentSettings tournamentSettings = tournament.getSettings();
		Player winner = tournament.playOff().getValue().getWinner().orElseThrow();

		System.out.println("-".repeat(20));
		System.out.println(String.format("Tournament %s(%s) ended", tournamentSettings.getName(), tournamentId));
		System.out.println("Players: " + tournament.getPlayers());
		System.out.println("Winner: " + winner);
		System.out.println("-".repeat(20));
	}

	private static void failIfTournamentNull(String tournamentId, Tournament tournament) {
		if (tournament == null) {
			throw new IllegalArgumentException(String.format("Tournament `%s` does not exists", tournamentId));
		}
	}
}
