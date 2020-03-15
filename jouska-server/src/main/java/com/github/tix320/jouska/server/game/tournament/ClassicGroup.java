package com.github.tix320.jouska.server.game.tournament;

import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.GameFactory;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.InGamePlayer;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public class ClassicGroup implements Group {

	private static final int WIN_POINTS = 3;
	private static final int DRAW_POINTS = 1;

	private final List<Player> players;

	private final Property<Map<Player, Integer>> groupPoints;

	private final List<Game> games;

	private final Property<Boolean> completed;

	public ClassicGroup(GameSettings gameSettings, List<Player> players) {
		if (players.size() < 2 || players.size() > 4) {
			throw new IllegalArgumentException("Players count must be [2,4]");
		}

		int playersCount = gameSettings.getPlayersCount();
		if (playersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic group game must be for two players",
							playersCount));
		}

		this.players = players;
		this.groupPoints = Property.forMap();
		this.completed = Property.forObject(false);
		this.games = generateGames(gameSettings);

		List<MonoObservable<List<InGamePlayer>>> onCompleteObservables = this.games.stream()
				.peek(this::listenGame)
				.map(Game::onComplete)
				.collect(Collectors.toList());

		Observable.zip(onCompleteObservables).subscribe(ignored -> completed.set(true));
	}

	@Override
	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public List<Game> games() {
		return Collections.unmodifiableList(games);
	}

	@Override
	public ReadOnlyProperty<Map<Player, Integer>> points() {
		return groupPoints.toReadOnly();
	}

	@Override
	public ReadOnlyProperty<Boolean> completed() {
		return completed.toReadOnly();
	}

	private List<Game> generateGames(GameSettings gameSettings) {
		List<Game> games = new ArrayList<>();
		for (int i = 0; i < players.size(); i++) {
			for (int j = i + 1; j < players.size(); j++) {
				Player firstPlayer = players.get(i);
				Player secondPlayer = players.get(j);
				Game game = GameFactory.create(gameSettings, Set.of(firstPlayer, secondPlayer));
				games.add(game);
			}
		}
		return games;
	}

	private void listenGame(Game game) {
		game.onComplete().subscribe(inGamePlayers -> {
			Map<InGamePlayer, Integer> statistics = game.getStatistics().summaryPoints().get();
			InGamePlayer firstPlayer = inGamePlayers.get(0);
			InGamePlayer secondPlayer = inGamePlayers.get(1);
			Integer firstSummaryPoints = statistics.get(firstPlayer);
			Integer secondSummaryPoints = statistics.get(secondPlayer);
			if (firstSummaryPoints.equals(secondSummaryPoints)) {
				addPointsToPlayer(firstPlayer.getPlayer(), DRAW_POINTS);
				addPointsToPlayer(secondPlayer.getPlayer(), DRAW_POINTS);
			}
			else if (firstSummaryPoints > secondSummaryPoints) {
				addPointsToPlayer(firstPlayer.getPlayer(), WIN_POINTS);
			}
			else {
				addPointsToPlayer(secondPlayer.getPlayer(), WIN_POINTS);
			}
		});
	}

	private void addPointsToPlayer(Player player, int points) {
		groupPoints.get().compute(player, (p, playerPoints) -> {
			if (playerPoints == null) {
				throw new IllegalStateException();
			}
			return playerPoints + points;
		});
	}
}
