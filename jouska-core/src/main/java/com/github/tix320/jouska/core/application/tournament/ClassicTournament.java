package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.creation.ClassicGroupSettings;
import com.github.tix320.jouska.core.application.game.creation.ClassicPlayOffSettings;
import com.github.tix320.jouska.core.application.game.creation.ClassicTournamentSettings;
import com.github.tix320.jouska.core.application.game.creation.TournamentSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ClassicTournament implements RestorableTournament {

	private final ClassicTournamentSettings settings;

	private final Set<Player> players;

	private final List<RestorableGroup> groups;

	private final Property<RestorablePlayOff> playOff;

	private final Property<TournamentState> state;

	public static ClassicTournament create(ClassicTournamentSettings settings) {
		return new ClassicTournament(settings);
	}

	private ClassicTournament(ClassicTournamentSettings settings) {
		this.settings = settings;
		this.players = new HashSet<>();
		this.groups = new ArrayList<>();
		this.playOff = Property.forObject();
		this.state = Property.forObject(TournamentState.INITIAL);
	}

	@Override
	public TournamentSettings getSettings() {
		return settings;
	}

	@Override
	public synchronized void addPlayer(Player player) {
		failIfStarted();

		players.add(player);
	}

	@Override
	public synchronized boolean removePlayer(Player player) {
		failIfStarted();

		return players.remove(player);
	}

	@Override
	public synchronized void start() {
		failIfStarted();

		int playersCount = players.size();
		if (playersCount < 4) {
			throw new IllegalArgumentException(
					String.format("Tournament players count must be >=4, but was %s", playersCount));
		}

		ClassicGroupSettings groupSettings = settings.getGroupSettings();
		List<RestorableGroup> groups = assembleRandomGroups(players, groupSettings);
		this.groups.addAll(groups);

		groups.forEach(Group::start);

		this.state.setValue(TournamentState.GROUP_STAGE);

		allGroupsCompleteness().subscribe(groupss -> {
			ClassicPlayOffSettings playOffSettings = settings.getPlayOffSettings();
			ClassicPlayOff classicPlayOff = createPlayOffFromGroups(groupss, playOffSettings);

			Property.inAtomicContext(() -> {
				state.setValue(TournamentState.PLAY_OFF_STAGE);
				playOff.setValue(classicPlayOff);
			});

			classicPlayOff.completed().subscribe(none -> state.setValue(TournamentState.COMPLETED));
		});
	}

	@Override
	public synchronized Set<Player> getPlayers() {
		return Collections.unmodifiableSet(players);
	}

	@Override
	public synchronized List<RestorableGroup> getGroups() {
		return Collections.unmodifiableList(groups);
	}

	@Override
	public ReadOnlyProperty<RestorablePlayOff> playOff() {
		return playOff.toReadOnly();
	}

	@Override
	public TournamentState getState() {
		return state.getValue();
	}

	@Override
	public MonoObservable<ClassicTournament> completed() {
		return state.asObservable().filter(state -> state == TournamentState.COMPLETED).map(state -> this).toMono();
	}

	@Override
	public Object getLock() {
		return this;
	}

	@Override
	public synchronized void restore(List<Group> groups, PlayOff playOff) throws RestoreException {
		for (Group group : groups) {
			if (!(group instanceof RestorableGroup)) {
				throw new RestoreException("Invalid type");
			}
		}
		if (playOff != null && !(playOff instanceof RestorablePlayOff)) {
			throw new RestoreException("Invalid type");
		}

		if (state.getValue() != TournamentState.INITIAL) {
			throw new RestoreException("Tournament already started");
		}

		int playersCount = players.size();
		if (playersCount < 4) {
			throw new RestoreException(String.format("Tournament players count must be >=4, but was %s", playersCount));
		}

		this.groups.addAll((List) groups);
		this.state.setValue(TournamentState.GROUP_STAGE);

		if (playOff == null) {
			allGroupsCompleteness().subscribe(groupss -> {
				ClassicPlayOffSettings playOffGameSettings = settings.getPlayOffSettings();
				ClassicPlayOff classicPlayOff = createPlayOffFromGroups(groupss, playOffGameSettings);

				Property.inAtomicContext(() -> {
					classicPlayOff.start();
					this.playOff.setValue(classicPlayOff);
					state.setValue(TournamentState.PLAY_OFF_STAGE);
				});

				classicPlayOff.completed().subscribe(none -> state.setValue(TournamentState.COMPLETED));
			});
		}
		else {
			this.playOff.setValue((RestorablePlayOff) playOff);
			playOff.completed().subscribe(none -> state.setValue(TournamentState.COMPLETED));
		}
	}

	private MonoObservable<List<Group>> allGroupsCompleteness() {
		List<MonoObservable<? extends Group>> completedObservables = this.groups.stream()
				.map(Group::completed)
				.collect(Collectors.toList());

		return Observable.zip(completedObservables).toMono();
	}

	private static ClassicPlayOff createPlayOffFromGroups(List<Group> groups, ClassicPlayOffSettings playOffSettings) {
		List<Iterator<Player>> groupWinners = groups.stream()
				.map(group -> group.getWinners().orElseThrow())
				.map(List::iterator)
				.collect(Collectors.toList());

		List<Player> playOffPlayers = new ArrayList<>();

		while (!groupWinners.isEmpty()) {
			Iterator<Iterator<Player>> iterator = groupWinners.iterator();

			while (iterator.hasNext()) {
				Iterator<Player> groupWinnersIterator = iterator.next();
				if (groupWinnersIterator.hasNext()) {
					playOffPlayers.add(groupWinnersIterator.next());
				}
				else {
					iterator.remove();
				}
			}
		}

		return ClassicPlayOff.create(playOffPlayers, playOffSettings);
	}

	private void failIfStarted() {
		TournamentState state = this.state.getValue();
		if (state != TournamentState.INITIAL) {
			throw new TournamentIllegalStateException("Tournament already started");
		}
	}

	private static List<RestorableGroup> assembleRandomGroups(Set<Player> playersSet,
															  ClassicGroupSettings groupSettings) {
		List<Player> players = new ArrayList<>(playersSet);

		Collections.shuffle(players);
		int playersCount = players.size();
		int groupsCount = resolveGroupCountForPlayers(playersCount);

		List<List<Player>> playersInGroups = distributePlayersInGroups(players, groupsCount);

		return playersInGroups.stream()
				.map(groupPlayers -> ClassicGroup.create(new HashSet<>(groupPlayers), groupSettings))
				.collect(Collectors.toUnmodifiableList());
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
}
