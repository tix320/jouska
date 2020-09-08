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
import com.github.tix320.jouska.server.infrastructure.origin.ServerTournamentOrigin;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.skimp.api.collection.Tuple;
import com.github.tix320.skimp.api.object.None;

public class TournamentManager {

	private final SinglePublisher<None> changesPublisher = new SinglePublisher<>(None.SELF);

	private final ServerTournamentOrigin tournamentOrigin;

	private final ClientPlayerMappingResolver clientPlayerMappingResolver;

	public TournamentManager(ServerTournamentOrigin tournamentOrigin,
							 ClientPlayerMappingResolver clientPlayerMappingResolver) {
		this.tournamentOrigin = tournamentOrigin;
		this.clientPlayerMappingResolver = clientPlayerMappingResolver;
	}

	public Observable<Collection<DBTournament>> tournaments() {
		return Observable.combineLatest(changesPublisher.asObservable(), DBTournament.all().asObservable())
				.map(Tuple::second)
				.map(Map::values);
	}

	public String createTournament(RestorableTournamentSettings settings, Player creator) {
		DBTournamentSettings tournamentSettings = DBTournamentSettings.wrap(settings, creator);

		DBTournament dbTournament = DBTournament.createNew(tournamentSettings);
		return dbTournament.getId();
	}

	public Confirmation joinTournament(String tournamentId, Player player) {
		DBTournament tournament = DBTournament.all().get(tournamentId);

		failIfTournamentNull(tournamentId, tournament);

		Player creator = tournament.getCreator();

		boolean canJoin = false;
		if (player.equals(creator)) {
			canJoin = true;
		}

		Long creatorClientId = clientPlayerMappingResolver.getClientIdByPlayer(creator.getId()).orElseThrow();

		TournamentJoinRequest request = new TournamentJoinRequest(
				new TournamentView(tournament.getId(), tournament.getSettings().getName(),
						tournament.getPlayers().size(), tournament.getSettings().getMaxPlayersCount(), creator, false),
				player);

		if (tournament.getPlayers().contains(player)) {
			canJoin = true;
		}
		else {
			try {
				Confirmation requestAnswer = tournamentOrigin.requestTournamentJoin(request, creatorClientId)
						.get(Duration.ofSeconds(30));

				System.out.println(requestAnswer);

				if (requestAnswer == Confirmation.ACCEPT) {
					canJoin = true;
				}
			}
			catch (TimeoutException | InterruptedException e) {
				canJoin = false;
			}
		}

		if (!canJoin) {
			return Confirmation.REJECT;
		}

		try {
			boolean added = tournament.addPlayer(player);
			if (!added) {
				throw new IllegalStateException("Already added");
			}
			changesPublisher.publish(None.SELF);
			return Confirmation.ACCEPT;
		}
		catch (TournamentIllegalStateException | GameAlreadyFullException e) {
			return Confirmation.REJECT;
		}

	}

	public Tournament getTournament(String tournamentId) {
		Tournament tournament = DBTournament.all().get(tournamentId);
		failIfTournamentNull(tournamentId, tournament);

		return tournament;
	}

	public void startTournament(String tournamentId) {
		DBTournament tournament = DBTournament.all().get(tournamentId);
		failIfTournamentNull(tournamentId, tournament);

		tournament.start();

		tournament.completed().subscribe(TournamentManager::onTournamentComplete);

		changesPublisher.publish(None.SELF);
	}

	private static void onTournamentComplete(DBTournament tournament) {
		String tournamentId = tournament.getId();
		TournamentSettings tournamentSettings = tournament.getSettings();
		Player winner = tournament.playOff().getValue().getWinner().orElseThrow();

		System.out.println("-".repeat(20));
		System.out.printf("Tournament %s(%s) ended%n", tournamentSettings.getName(), tournamentId);
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
