package com.github.tix320.jouska.server.service.application;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.MapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.IDGenerator;

public class TournamentManager {

	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final MapProperty<Long, TournamentInfo> tournaments = Property.forMap(new ConcurrentHashMap<>());

	public static long createNewTournament(CreateTournamentCommand createTournamentCommand) {
		long tournamentId = ID_GENERATOR.next();
		tournaments.put(tournamentId,
				new TournamentInfo(tournamentId, createTournamentCommand.getTournamentSettings()));
		return tournamentId;
	}

	public static Observable<Collection<TournamentInfo>> tournaments() {
		return tournaments.asObservable().map(Map::values);
	}

	public static TournamentInfo getTournament(long tournamentId) {
		TournamentInfo tournamentInfo = tournaments.get(tournamentId);
		failIfTournamentNull(tournamentId, tournamentInfo);
		return tournamentInfo;
	}

	public static void joinTournament(long tournamentId) {
		TournamentInfo tournamentInfo = tournaments.get(tournamentId);
		failIfTournamentNull(tournamentId, tournamentInfo);

		int maxPlayersCount = tournamentInfo.getTournamentSettings().getPlayersCount();
	}

	private static void failIfTournamentNull(long tournamentId, TournamentInfo tournamentInfo) {
		if (tournamentInfo == null) {
			throw new IllegalArgumentException(String.format("Tournament `%s` does not exists", tournamentId));
		}
	}

}
