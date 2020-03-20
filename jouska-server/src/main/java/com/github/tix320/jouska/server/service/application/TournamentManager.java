package com.github.tix320.jouska.server.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.server.model.TournamentInfo;
import com.github.tix320.kiwi.api.util.IDGenerator;

public class TournamentManager {

	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final Map<Long, TournamentInfo> tournaments = new ConcurrentHashMap<>();

	public static long createNewTournament(CreateTournamentCommand createTournamentCommand) {
		long tournamentId = ID_GENERATOR.next();
		GameSettings groupGameSettings = createTournamentCommand.getGroupSettings();
		GameSettings playOffGameSettings = createTournamentCommand.getPlayOffSettings();
		tournaments.put(tournamentId, new TournamentInfo(tournamentId, createTournamentCommand.getName(),
				createTournamentCommand.getPlayersCount()));
		return tournamentId;
	}

	public static List<TournamentInfo> getTournaments() {
		return new ArrayList<>(tournaments.values());
	}

	public static TournamentInfo getTournament(long tournamentId) {
		return tournaments.get(tournamentId);
	}

}
