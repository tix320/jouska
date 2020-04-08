package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GameWithSettings;
import com.github.tix320.jouska.core.application.game.TournamentState;
import com.github.tix320.jouska.core.application.game.creation.GameFactory;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.MathUtils;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.publisher.CachedPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;

public class ClassicPlayOff implements PlayOff {

	private final GameSettings baseSettings;

	private final List<Player> players;

	private final List<List<PlayOffGame>> games;

	private final CachedPublisher<GameWithSettings> createdGamesPublisher;

	private final Property<TournamentState> playOffState;

	private final AtomicReference<Player> winner;

	public ClassicPlayOff(GameSettings baseSettings, List<Player> players) {
		int playersCount = players.size();
		if (playersCount < 2) {
			throw new IllegalArgumentException("Players count must be >=2");
		}

		int gamePlayersCount = baseSettings.getPlayersCount();
		if (gamePlayersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic play off game must be for two players",
							gamePlayersCount));
		}

		this.baseSettings = baseSettings;
		this.playOffState = Property.forObject(TournamentState.IN_PROGRESS);
		this.players = Collections.unmodifiableList(players);
		this.createdGamesPublisher = Publisher.cached();
		this.games = initGamesSpace(playersCount);
		this.winner = new AtomicReference<>();
		fillFirstTour();
	}

	@Override
	public List<Player> getPlayers() {
		return players;
	}

	@Override
	public List<List<PlayOffGame>> getGamesStructure() {
		return games.stream()
				.map(ArrayList::new)
				.collect(Collectors.toUnmodifiableList()); // return deep immutable copy
	}

	@Override
	public Observable<GameWithSettings> createdGames() {
		return createdGamesPublisher.asObservable();
	}

	@Override
	public MonoObservable<None> completed() {
		return playOffState.asObservable()
				.filter(tournamentState -> tournamentState == TournamentState.COMPLETED)
				.map(tournamentState -> None.SELF)
				.toMono();
	}

	@Override
	public Optional<Player> getWinner() {
		return Optional.ofNullable(winner.get());
	}

	private void fillFirstTour() {
		Iterator<Player> playersIterator = players.iterator();

		List<PlayOffGame> firstTourGames = this.games.get(0);

		if (players.size() % 2 == 0) {
			for (int i = 0; i < firstTourGames.size(); i++) {
				Player firstPlayer = playersIterator.next();
				Player secondPlayer = playersIterator.next();
				createNewGame(firstPlayer, secondPlayer, 0, i);
			}
		}
		else {
			for (int i = 0; i < firstTourGames.size() - 1; i++) {
				Player firstPlayer = playersIterator.next();
				Player secondPlayer = playersIterator.next();

				createNewGame(firstPlayer, secondPlayer, 0, i);
			}

			int lastGameIndex = firstTourGames.size() - 1;
			Player singlePlayer = playersIterator.next();
			createNewGame(singlePlayer, null, 0, lastGameIndex);
		}
	}

	private void onGameComplete(int tourIndex, int tourGameIndex, Game game) {
		Player winnerOfPreviousGame = game.getWinner().orElseThrow().getRealPlayer();

		int toursCount = games.size();
		if (tourIndex == toursCount - 1) { // last tour, last game
			this.winner.set(winnerOfPreviousGame);
			playOffState.setValue(TournamentState.COMPLETED);
		}
		else {
			int nextTourIndex = tourIndex + 1;
			List<PlayOffGame> nextTourGames = games.get(nextTourIndex);
			int nextTourGameIndex = tourGameIndex / 2; // 0 -> 0, 1 -> 0, 2 -> 1, 3 -> 1, 4 -> 2, 5 -> 2 ...
			boolean mustBeFirstPlayer = tourGameIndex % 2 == 0;

			PlayOffGame playOffGame = nextTourGames.get(nextTourGameIndex);
			if (playOffGame.getFirstPlayer() != null && playOffGame.getSecondPlayer() != null) {
				throw new IllegalStateException();
			}

			if (playOffGame.getFirstPlayer() != null) {
				if (mustBeFirstPlayer) {
					throw new IllegalStateException();
				}
				else {
					createNewGame(playOffGame.getFirstPlayer(), winnerOfPreviousGame, nextTourIndex, nextTourGameIndex);
				}
			}
			else if (playOffGame.getSecondPlayer() != null) {
				if (mustBeFirstPlayer) {
					createNewGame(winnerOfPreviousGame, playOffGame.getSecondPlayer(), nextTourIndex,
							nextTourGameIndex);
				}
				else {
					throw new IllegalStateException();
				}
			}
			else {
				if (mustBeFirstPlayer) {
					nextTourGames.set(nextTourGameIndex, new PlayOffGame(winnerOfPreviousGame, null, null));
				}
				else {
					nextTourGames.set(nextTourGameIndex, new PlayOffGame(null, winnerOfPreviousGame, null));
				}
			}
		}
	}

	private void createNewGame(Player firstPlayer, Player secondPlayer, int tourIndex, int tourGameIndex) {
		Objects.requireNonNull(firstPlayer);

		GameWithSettings gameWithSettings;
		PlayOffGame playOffGame;
		if (secondPlayer == null) {
			GameSettings gameSettings = this.baseSettings.changeName(String.format("%s VS <None>", firstPlayer))
					.changePlayersCount(1);
			Game game = GameFactory.create(gameSettings, Set.of(firstPlayer));
			game.start();
			game.forceCompleteGame(firstPlayer);
			gameWithSettings = new GameWithSettings(game, gameSettings);
			playOffGame = new PlayOffGame(firstPlayer, null, gameWithSettings);
		}
		else {
			GameSettings gameSettings = this.baseSettings.changeName(
					String.format("%s VS %s", firstPlayer, secondPlayer));
			Game game = GameFactory.create(gameSettings, Set.of(firstPlayer, secondPlayer));
			gameWithSettings = new GameWithSettings(game, gameSettings);
			playOffGame = new PlayOffGame(firstPlayer, secondPlayer, gameWithSettings);
		}

		games.get(tourIndex).set(tourGameIndex, playOffGame);

		gameWithSettings.getGame().completed().subscribe(game -> onGameComplete(tourIndex, tourGameIndex, game));
		createdGamesPublisher.publish(gameWithSettings);
	}

	private static List<List<PlayOffGame>> initGamesSpace(int playersCount) {
		int firstTourGamesCount;
		if (MathUtils.isPowerOfTwo(playersCount)) {
			firstTourGamesCount = playersCount / 2;
		}
		else {
			firstTourGamesCount = MathUtils.nextPowerOf2(playersCount) / 2;
		}

		List<List<PlayOffGame>> games = new ArrayList<>();
		for (int i = firstTourGamesCount; i != 0; i /= 2) { // i [8,4,2,1]
			List<PlayOffGame> tourGames = new ArrayList<>(i);
			for (int j = 0; j < i; j++) {
				tourGames.add(new PlayOffGame(null, null, null));
			}
			games.add(tourGames);
		}
		return Collections.unmodifiableList(games);
	}
}
