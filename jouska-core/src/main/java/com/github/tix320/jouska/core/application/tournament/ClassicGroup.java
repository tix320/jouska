package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GamePlayer;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.creation.ClassicGroupSettings;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGroupSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.property.Property;
import com.github.tix320.kiwi.property.StateProperty;

public class ClassicGroup implements RestorableGroup {

	private static final int WIN_POINTS = 1;

	private final List<GroupPlayer> players;

	private final ClassicGroupSettings settings;

	private final List<Game> games;

	private final StateProperty<GroupState> state;

	private final AtomicReference<List<Player>> winners;

	public static ClassicGroup create(Set<Player> players, ClassicGroupSettings settings) {
		GameSettings baseGameSettings = settings.getBaseGameSettings();

		if (players.size() < 2 || players.size() > 4) {
			throw new IllegalArgumentException("Players count must be [2,4]");
		}

		int gamePLayersCount = baseGameSettings.getPlayersCount();
		if (gamePLayersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic group games must be for two players",
							gamePLayersCount));
		}

		return new ClassicGroup(players, settings);
	}

	private ClassicGroup(Set<Player> players, ClassicGroupSettings settings) {
		this.players = players.stream().map(GroupPlayer::new).collect(Collectors.toList());
		this.settings = settings;
		this.games = new ArrayList<>();
		this.state = Property.forState(GroupState.INITIAL);
		this.winners = new AtomicReference<>();
	}

	@Override
	public synchronized void start() {
		failIfStarted();

		List<Game> games = generateGames(players.stream().map(GroupPlayer::getPlayer).collect(Collectors.toSet()),
				settings.getBaseGameSettings());
		state.setValue(GroupState.RUNNING);

		games.forEach(this::registerGame);

		subscribeToAllGamesComplete();
	}

	@Override
	public List<GroupPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public synchronized Optional<List<Player>> getWinners() {
		return Optional.ofNullable(winners.get());
	}

	@Override
	public synchronized List<Game> getGames() {
		if (state.getValue() == GroupState.INITIAL) {
			throw new TournamentIllegalStateException("Group not started");
		}
		return Collections.unmodifiableList(games);
	}

	@Override
	public MonoObservable<ClassicGroup> completed() {
		return state.asObservable()
				.filter(groupState -> groupState == GroupState.COMPLETED)
				.map(tournamentState -> this)
				.toMono();
	}

	@Override
	public RestorableGroupSettings getSettings() {
		return settings;
	}

	@Override
	public void restore(List<Game> games) throws RestoreException {
		state.checkState(GroupState.INITIAL);
		int gamesCount = groupGamesCountByPlayersCount(players.size());
		if (gamesCount != games.size()) {
			throw new IllegalStateException();
		}

		for (Game game : games) {
			registerGame(game);
		}
		state.setValue(GroupState.RUNNING);

		subscribeToAllGamesComplete();
	}

	private int groupGamesCountByPlayersCount(int playersCount) {
		return (playersCount * (playersCount - 1)) / 2;
	}

	private void subscribeToAllGamesComplete() {
		allGamesCompleteness().subscribe(games1 -> onAllGamesComplete());
	}

	private MonoObservable<List<Game>> allGamesCompleteness() {
		List<MonoObservable<? extends Game>> completedObservables = this.games.stream()
				.map(Game::completed)
				.collect(Collectors.toList());

		return Observable.zip(completedObservables).toMono();
	}

	private void registerGame(Game game) {
		games.add(game);
		game.completed().subscribe(this::handleGameComplete);
	}

	private void handleGameComplete(Game game) {
		GamePlayer winner = game.getWinner().orElseThrow();
		GamePlayer loser = game.getLosers().get(0);
		Map<GamePlayer, Integer> gameSummaryPoints = game.getStatistics().summaryPoints();

		GroupPlayer gameWinner = findGroupPlayer(winner.getRealPlayer());
		GroupPlayer gameLoser = findGroupPlayer(winner.getRealPlayer());

		gameWinner.addGroupPoints(WIN_POINTS);
		gameWinner.addTotalGamesPoints(gameSummaryPoints.get(winner));

		gameLoser.addTotalGamesPoints(gameSummaryPoints.get(loser));
	}

	private void onAllGamesComplete() {
		List<Player> players = determineGroupWinners();
		winners.set(players);
		state.setValue(GroupState.COMPLETED);
	}

	private List<Player> determineGroupWinners() {
		List<GroupPlayer> sortedPlayers = players.stream()
				.sorted(Comparator.comparing(GroupPlayer::getGroupPoints)
						.thenComparing(GroupPlayer::getTotalGamesPoints)
						.reversed())
				.collect(Collectors.toList());


		if (sortedPlayers.size() == 2) {
			return List.of(sortedPlayers.get(0).getPlayer());
		}
		else { // >2
			return List.of(sortedPlayers.get(0).getPlayer(), sortedPlayers.get(1).getPlayer());
		}
	}

	private GroupPlayer findGroupPlayer(Player player) {
		for (GroupPlayer groupPlayer : players) {
			if (groupPlayer.getPlayer().equals(player)) {
				return groupPlayer;
			}
		}
		throw new IllegalStateException(player + "");
	}

	private void failIfStarted() {
		GroupState state = this.state.getValue();
		if (state != GroupState.INITIAL) {
			throw new TournamentIllegalStateException("Group already started");
		}
	}

	private static List<Game> generateGames(Set<Player> players, GameSettings gameSettings) {
		List<Player> playersList = new ArrayList<>(players);
		List<Game> games = new ArrayList<>();
		for (int i = 0; i < playersList.size(); i++) {
			for (int j = i + 1; j < playersList.size(); j++) {
				Player firstPlayer = playersList.get(i);
				Player secondPlayer = playersList.get(j);
				gameSettings = gameSettings.changeName(
						String.format("%s VS %s", firstPlayer.getNickname(), secondPlayer.getNickname()));

				Game game = gameSettings.createGame();
				game.addPlayer(new GamePlayer(firstPlayer, PlayerColor.RED));
				game.addPlayer(new GamePlayer(secondPlayer, PlayerColor.BLUE));
				game.shufflePLayers();
				games.add(game);
			}
		}
		return games;
	}

	public static class GroupPlayer {
		private final Player player;
		private final AtomicInteger groupPoints;
		private final AtomicInteger totalGamesPoints;

		private GroupPlayer(Player player) {
			this.player = player;
			this.groupPoints = new AtomicInteger(0);
			this.totalGamesPoints = new AtomicInteger(0);
		}

		public Player getPlayer() {
			return player;
		}

		public int getGroupPoints() {
			return groupPoints.get();
		}

		public int getTotalGamesPoints() {
			return totalGamesPoints.get();
		}

		private void addGroupPoints(int points) {
			groupPoints.addAndGet(points);
		}

		private void addTotalGamesPoints(int points) {
			totalGamesPoints.addAndGet(points);
		}
	}
}
