package com.github.tix320.jouska.server.game.tournament;

import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.util.collection.Tuple;

public class ClassicTournament implements Tournament {

	private final GameSettings groupGameSettings;

	private final GameSettings playOffGameSettings;

	private final List<Group> groups;

	private final Property<PlayOff> playOff;

	public ClassicTournament(GameSettings groupGameSettings, GameSettings playOffGameSettings, List<Player> players) {
		this.groupGameSettings = groupGameSettings;
		this.playOffGameSettings = playOffGameSettings;
		if (players.size() < 4) {
			throw new IllegalArgumentException("Tournament players count must be >=4");
		}
		this.groups = assembleGroups(players);
		this.playOff = Property.forObject();
		listenGroupsCompleteness();
	}

	@Override
	public List<Group> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	@Override
	public ReadOnlyProperty<PlayOff> playOff() {
		return playOff.toReadOnly();
	}

	private List<Group> assembleGroups(List<Player> players) {
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
		List<MonoObservable<Boolean>> groupsCompleteObservables = this.groups.stream()
				.map(group -> group.completed().asObservable().filter(completed -> completed).toMono())
				.collect(Collectors.toList());
		Observable.zip(groupsCompleteObservables).subscribe(ignored -> {
			List<Player> groupWinners = this.groups.stream()
					.flatMap(group -> resolveGroupWinners(group).stream())
					.collect(Collectors.toList());

			ClassicPlayOff classicPlayOff = new ClassicPlayOff(playOffGameSettings, groupWinners);
			playOff.set(classicPlayOff);
		});

	}

	private List<Player> resolveGroupWinners(Group group) {
		Map<Player, Integer> groupPoints = group.points().get();

		List<Tuple<Player, Integer>> sortedPlayers = groupPoints.entrySet()
				.stream()
				.map(entry -> new Tuple<>(entry.getKey(), entry.getValue()))
				.sorted(Comparator.<Tuple<Player, Integer>, Integer>comparing(Tuple::second).reversed())
				.collect(Collectors.toList());

		if (sortedPlayers.size() == 2) {
			return List.of(sortedPlayers.get(0).first(), sortedPlayers.get(1).first());
		}
		else { // >2
			Tuple<Player, Integer> firstPlace = sortedPlayers.get(0);
			Tuple<Player, Integer> secondPlace = sortedPlayers.get(1);
			Tuple<Player, Integer> thirdPlace = sortedPlayers.get(2);
			if (secondPlace.second() > thirdPlace.second()) {
				return List.of(firstPlace.first(), secondPlace.first());
			}
			else {
				return List.of();
				// TODO
			}
		}


	}
}
