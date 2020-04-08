package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GameWithSettings;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.application.game.TournamentState;
import com.github.tix320.jouska.core.application.game.creation.GameFactory;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.None;

public class ClassicGroup implements Group {

	private static final int WIN_POINTS = 1;

	private final List<GroupPlayer> players;

	private final List<GameWithSettings> games;

	private final Property<TournamentState> groupState;

	private final AtomicReference<List<Player>> winners;

	public ClassicGroup(GameSettings gameSettings, List<Player> players) {
		if (players.size() < 2 || players.size() > 4) {
			throw new IllegalArgumentException("Players count must be [2,4]");
		}

		int gamePLayersCount = gameSettings.getPlayersCount();
		if (gamePLayersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic group game must be for two players",
							gamePLayersCount));
		}

		this.players = players.stream().map(GroupPlayer::new).collect(Collectors.toList());
		this.groupState = Property.forObject(TournamentState.IN_PROGRESS);
		this.games = generateGames(players, gameSettings);
		this.winners = new AtomicReference<>();

		List<MonoObservable<? extends Game>> onCompleteObservables = this.games.stream()
				.map(gameWithSettings -> gameWithSettings.getGame()
						.completed()
						.peek(ignored -> handleGameComplete(gameWithSettings.getGame()))
						.toMono())
				.collect(Collectors.toList());

		Observable.zip(onCompleteObservables).subscribe(ignored -> {
			winners.set(resolveGroupWinners());
			groupState.setValue(TournamentState.COMPLETED);
		});
	}

	@Override
	public List<GroupPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public Optional<List<Player>> getWinners() {
		return Optional.ofNullable(winners.get());
	}

	@Override
	public List<GameWithSettings> getGames() {
		return Collections.unmodifiableList(games);
	}

	@Override
	public MonoObservable<None> completed() {
		return groupState.asObservable()
				.filter(tournamentState -> tournamentState == TournamentState.COMPLETED)
				.map(tournamentState -> None.SELF)
				.toMono();
	}

	private List<GameWithSettings> generateGames(List<Player> players, GameSettings gameSettings) {
		List<GameWithSettings> games = new ArrayList<>();
		for (int i = 0; i < players.size(); i++) {
			for (int j = i + 1; j < players.size(); j++) {
				Player firstPlayer = players.get(i);
				Player secondPlayer = players.get(j);
				gameSettings = gameSettings.changeName(
						String.format("%s VS %s", firstPlayer.getNickname(), secondPlayer.getNickname()));

				Game game = GameFactory.create(gameSettings, Set.of(firstPlayer, secondPlayer));
				games.add(new GameWithSettings(game, gameSettings));
			}
		}
		return games;
	}

	private void handleGameComplete(Game game) {
		InGamePlayer winner = game.getWinner().orElseThrow();
		InGamePlayer loser = game.getLosers().get(0);
		Map<InGamePlayer, Integer> gameSummaryPoints = game.getStatistics().summaryPoints();

		GroupPlayer gameWinner = findGroupPlayer(winner.getRealPlayer());
		GroupPlayer gameLoser = findGroupPlayer(winner.getRealPlayer());

		gameWinner.addGroupPoints(WIN_POINTS);
		gameWinner.addTotalGamesPoints(gameSummaryPoints.get(winner));

		gameLoser.addTotalGamesPoints(gameSummaryPoints.get(loser));
	}

	private List<Player> resolveGroupWinners() {
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

		private synchronized void addGroupPoints(int points) {
			groupPoints.addAndGet(points);
		}

		private synchronized void addTotalGamesPoints(int points) {
			totalGamesPoints.addAndGet(points);
		}
	}
}
