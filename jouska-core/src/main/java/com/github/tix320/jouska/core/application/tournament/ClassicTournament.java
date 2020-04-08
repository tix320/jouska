package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.TournamentState;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.TournamentSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.None;

public class ClassicTournament implements Tournament {

	private final GameSettings groupGameSettings;

	private final GameSettings playOffGameSettings;

	private final Set<Player> players;

	private final List<Group> groups;

	private final Property<PlayOff> playOff;

	private final Property<TournamentState> tournamentState;

	public ClassicTournament(TournamentSettings tournamentSettings, Set<Player> players) {
		this.groupGameSettings = tournamentSettings.getGroupGameSettings();
		this.playOffGameSettings = tournamentSettings.getPlayOffGameSettings();
		if (players.size() < 4) {
			throw new IllegalArgumentException("Tournament players count must be >=4");
		}
		this.players = Set.copyOf(players);
		this.groups = assembleGroups(players);
		this.playOff = Property.forObject();
		this.tournamentState = Property.forObject(TournamentState.IN_PROGRESS);
		listenGroupsCompleteness();
	}

	@Override
	public Set<Player> getPlayers() {
		return players;
	}

	@Override
	public List<Group> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	@Override
	public MonoObservable<PlayOff> playOff() {
		return playOff.asObservable().toMono();
	}

	private List<Group> assembleGroups(Set<Player> playersSet) {
		List<Player> players = new ArrayList<>(playersSet);

		Collections.shuffle(players);
		int playersCount = players.size();
		int groupsCount = resolveGroupCountForPlayers(playersCount);

		List<List<Player>> playersInGroups = distributePlayersInGroups(players, groupsCount);

		return playersInGroups.stream()
				.map(groupPlayers -> new ClassicGroup(groupGameSettings, groupPlayers))
				.collect(Collectors.toList());
	}

	private static int resolveGroupCountForPlayers(int playersCount) {
		int minimumGroupsCount = playersCount / 4;
		int groupsCount;
		if (playersCount % 4 == 0) {
			groupsCount = minimumGroupsCount;
		}
		else {
			groupsCount = minimumGroupsCount + 1;
		}
		return groupsCount;
	}

	private static List<List<Player>> distributePlayersInGroups(List<Player> players, int groupsCount) {
		Iterator<Player> playerIterator = players.iterator();

		List<List<Player>> allGroupPlayers = new ArrayList<>(groupsCount);
		for (int i = 0; i < groupsCount; i++) {
			allGroupPlayers.add(new ArrayList<>());
		}

		while (playerIterator.hasNext()) {
			for (List<Player> groupPlayers : allGroupPlayers) {
				if (playerIterator.hasNext()) {
					Player next = playerIterator.next();
					groupPlayers.add(next);
				}
				else {
					break;
				}
			}
		}

		return allGroupPlayers;
	}

	private void listenGroupsCompleteness() {
		List<MonoObservable<None>> groupsCompleteObservables = this.groups.stream()
				.map(Group::completed)
				.collect(Collectors.toList());
		Observable.zip(groupsCompleteObservables).subscribe(ignored -> {
			List<Player> groupWinners = this.groups.stream()
					.flatMap(group -> group.getWinners().orElseThrow().stream())
					.collect(Collectors.toList());

			ClassicPlayOff classicPlayOff = new ClassicPlayOff(playOffGameSettings,
					groupWinners); //TODO distribute group winners fairly in play-off
			playOff.setValue(classicPlayOff);
			listenPlayOffCompleteness();
		});
	}

	private void listenPlayOffCompleteness() {
		PlayOff playOff = this.playOff.getValue();
		playOff.completed().subscribe(none -> tournamentState.setValue(TournamentState.COMPLETED));
	}
}
