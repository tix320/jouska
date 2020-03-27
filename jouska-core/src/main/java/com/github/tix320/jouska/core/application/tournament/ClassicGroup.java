package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.creation.GameFactory;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.util.None;

public class ClassicGroup implements Group {

	private static final int WIN_POINTS = 1;

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
		this.groupPoints = Property.forObject();
		this.completed = Property.forObject(false);
		this.games = generateGames(gameSettings);

		List<MonoObservable<None>> onCompleteObservables = this.games.stream()
				.peek(this::listenGame)
				.map(game -> game.completed().toMono())
				.collect(Collectors.toList());

		Observable.zip(onCompleteObservables).subscribe(ignored -> completed.setValue(true));
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
		game.completed().subscribe(ignored -> {
			InGamePlayer winner = game.getWinner().get();
			addPointsToPlayer(winner.getRealPlayer());
		});
	}

	private void addPointsToPlayer(Player player) {
		groupPoints.getValue().compute(player, (p, playerPoints) -> {
			if (playerPoints == null) {
				throw new IllegalStateException();
			}
			return playerPoints + ClassicGroup.WIN_POINTS;
		});
	}
}
