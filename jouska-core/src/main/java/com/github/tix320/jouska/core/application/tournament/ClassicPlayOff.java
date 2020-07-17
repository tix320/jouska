package com.github.tix320.jouska.core.application.tournament;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GamePlayer;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.creation.ClassicPlayOffSettings;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorablePlayOffSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.MathUtils;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;

public class ClassicPlayOff implements RestorablePlayOff {

	private final ClassicPlayOffSettings settings;

	private final List<Player> players;

	private final List<List<PlayOffGame>> tours;

	private final Property<PlayOffState> state;

	private final AtomicReference<Player> winner;

	private final Publisher<None> changes;

	public static ClassicPlayOff create(List<Player> players, ClassicPlayOffSettings settings) {
		int playersCount = players.size();
		if (playersCount < 2) {
			throw new IllegalArgumentException("Players count must be more than 1");
		}

		int gamePlayersCount = settings.getBaseGameSettings().getPlayersCount();
		if (gamePlayersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic play off game must be for two players",
							gamePlayersCount));
		}

		return new ClassicPlayOff(players, settings);
	}


	private ClassicPlayOff(List<Player> players, ClassicPlayOffSettings settings) {
		this.settings = settings;
		this.state = Property.forObject(PlayOffState.INITIAL);
		this.players = List.copyOf(players);
		this.tours = initGamesSpace(players.size());
		this.winner = new AtomicReference<>();
		this.changes = Publisher.simple();
	}

	@Override
	public synchronized void start() {
		failIfStarted();

		fillFirstTour();
		state.setValue(PlayOffState.RUNNING);
	}

	@Override
	public RestorablePlayOffSettings getSettings() {
		return settings;
	}

	@Override
	public List<Player> getPlayers() {
		return players;
	}

	@Override
	public synchronized List<List<PlayOffGame>> getTours() {
		return tours.stream().map(List::copyOf).collect(Collectors.toUnmodifiableList()); // return deep immutable copy
	}

	@Override
	public MonoObservable<None> completed() {
		return state.asObservable()
				.filter(playOffState -> playOffState == PlayOffState.COMPLETED)
				.map(tournamentState -> None.SELF)
				.toMono();
	}

	@Override
	public Optional<Player> getWinner() {
		return Optional.ofNullable(winner.get());
	}

	@Override
	public Observable<None> changes() {
		return changes.asObservable();
	}

	@Override
	public synchronized void restore(List<List<PlayOffGame>> structure) throws RestoreException {
		if (state.getValue() != PlayOffState.INITIAL) {
			throw new RestoreException("Play-off already started");
		}

		try {
			List<List<PlayOffGame>> tours = this.tours;
			for (int i = 0; i < tours.size(); i++) {
				List<PlayOffGame> tour = tours.get(i);
				for (int j = 0; j < tour.size(); j++) {
					PlayOffGame playOffGameToReplace = Objects.requireNonNull(structure.get(i).get(j));
					tour.set(j, playOffGameToReplace);
				}
			}

			for (int i = 0; i < tours.size(); i++) {
				List<PlayOffGame> tour = tours.get(i);
				for (int j = 0; j < tour.size(); j++) {
					int tourIndex = i;
					int tourGameIndex = j;
					PlayOffGame playOffGame = tour.get(j);
					if (playOffGame.getRealPLayersToBe() != 0) {
						Game game = playOffGame.getGame();
						if (game != null && !game.isCompleted()) {
							game.completed()
									.subscribe(g -> onGameComplete(tourIndex, tourGameIndex,
											g.getWinner().orElseThrow().getRealPlayer()));
						}
					}
				}
			}
			state.setValue(PlayOffState.RUNNING);

			Game lastGame = tours.get(tours.size() - 1).get(0).getGame();
			if (lastGame != null && lastGame.isCompleted()) {
				winner.set(lastGame.getWinner().orElseThrow().getRealPlayer());
				state.setValue(PlayOffState.COMPLETED);
			}
		}
		catch (RuntimeException e) {
			throw new RestoreException("Failed to restore.", e);
		}

	}

	private void fillFirstTour() {
		ListIterator<Player> playersIterator = players.listIterator();

		List<List<PlayOffGame>> tours = this.tours;
		List<PlayOffGame> firstTourGames = tours.get(0);

		for (int i = 0; i < firstTourGames.size(); i++) {
			PlayOffGame playOffGame = firstTourGames.get(i);
			switch (playOffGame.getRealPLayersToBe()) {
				case 0:
					break;
				case 1:
					Player singlePlayer = playersIterator.next();
					PlayOffGame newGame = createNewGame(singlePlayer, null);
					registerGame(newGame, 0, i);
					break;
				case 2:
					Player firstPlayer = playersIterator.next();
					Player secondPlayer = playersIterator.next();
					newGame = createNewGame(firstPlayer, secondPlayer);
					registerGame(newGame, 0, i);
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}

	private void onGameComplete(int tourIndex, int tourGameIndex, Player winnerOfPreviousGame) {
		Objects.requireNonNull(winnerOfPreviousGame);
		List<List<PlayOffGame>> tours = this.tours;
		int toursCount = tours.size();
		if (tourIndex == toursCount - 1) { // last tour, last game
			winner.set(winnerOfPreviousGame);
			state.setValue(PlayOffState.COMPLETED);
		}
		else {
			int nextTourIndex = tourIndex + 1;
			int nextTourGameIndex = tourGameIndex / 2; // 0 -> 0, 1 -> 0, 2 -> 1, 3 -> 1, 4 -> 2, 5 -> 2 ...
			boolean mustBeFirstPlayer = tourGameIndex % 2 == 0;

			synchronized (this.tours) {
				List<PlayOffGame> nextTourGames = tours.get(nextTourIndex);
				PlayOffGame playOffGame = nextTourGames.get(nextTourGameIndex);
				if (playOffGame.getFirstPlayer() != null && playOffGame.getSecondPlayer() != null) {
					throw new IllegalStateException();
				}

				if (playOffGame.getFirstPlayer() != null) {
					if (mustBeFirstPlayer) {
						throw new IllegalStateException();
					}
					else {
						PlayOffGame newGame = createNewGame(playOffGame.getFirstPlayer(), winnerOfPreviousGame);
						registerGame(newGame, nextTourIndex, nextTourGameIndex);
					}
				}
				else if (playOffGame.getSecondPlayer() != null) {
					if (mustBeFirstPlayer) {
						PlayOffGame newGame = createNewGame(winnerOfPreviousGame, playOffGame.getSecondPlayer());
						registerGame(newGame, nextTourIndex, nextTourGameIndex);
					}
					else {
						throw new IllegalStateException();
					}
				}
				else if (playOffGame.getRealPLayersToBe() == 1) {
					PlayOffGame newGame;
					if (mustBeFirstPlayer) {
						newGame = createNewGame(winnerOfPreviousGame, null);
					}
					else {
						newGame = createNewGame(null, winnerOfPreviousGame);
					}
					registerGame(newGame, nextTourIndex, nextTourGameIndex);
				}
				else {
					if (mustBeFirstPlayer) {
						nextTourGames.set(nextTourGameIndex, new PlayOffGame(winnerOfPreviousGame, null, null, 2));
					}
					else {
						nextTourGames.set(nextTourGameIndex, new PlayOffGame(null, winnerOfPreviousGame, null, 2));
					}
				}
			}
			changes.publish(None.SELF);
		}
	}

	private PlayOffGame createNewGame(Player firstPlayer, Player secondPlayer) {
		if (firstPlayer == null && secondPlayer == null) {
			throw new NullPointerException();
		}

		GameSettings baseGameSettings = this.settings.getBaseGameSettings();

		if (firstPlayer != null && secondPlayer != null) {
			GameSettings gameSettings = baseGameSettings.changeName(
					String.format("%s VS %s", firstPlayer, secondPlayer));
			Game game = gameSettings.createGame();
			game.addPlayer(new GamePlayer(firstPlayer, PlayerColor.RED));
			game.addPlayer(new GamePlayer(secondPlayer, PlayerColor.BLUE));
			game.shufflePLayers();
			return new PlayOffGame(firstPlayer, secondPlayer, game, 2);
		}
		else {
			Player singlePlayer = firstPlayer == null ? secondPlayer : firstPlayer;

			GameSettings gameSettings = baseGameSettings.changeName(String.format("%s VS <None>", singlePlayer))
					.changePlayersCount(1);
			Game game = gameSettings.createGame();
			game.addPlayer(new GamePlayer(singlePlayer, PlayerColor.RED));
			game.start();
			game.forceCompleteGame(singlePlayer);
			return new PlayOffGame(singlePlayer, null, game, 1);
		}
	}

	private void registerGame(PlayOffGame playOffGame, int tourIndex, int tourGameIndex) {
		tours.get(tourIndex).set(tourGameIndex, playOffGame);

		Game game = playOffGame.getGame();
		game.completed()
				.subscribe(g -> onGameComplete(tourIndex, tourGameIndex, g.getWinner().orElseThrow().getRealPlayer()));
	}

	private void failIfStarted() {
		PlayOffState state = this.state.getValue();
		if (state != PlayOffState.INITIAL) {
			throw new TournamentIllegalStateException("Play-off already started");
		}
	}

	private static List<List<PlayOffGame>> initGamesSpace(int playersCount) {
		int firstTourGamesCount = MathUtils.nextPowerOf2(playersCount) / 2;

		List<List<PlayOffGame>> games = new ArrayList<>();
		for (int i = firstTourGamesCount; i != 0; i /= 2) { // i [8,4,2,1]
			int tourPlayersCount = playersCount;
			List<PlayOffGame> tourGames = new ArrayList<>(i);
			for (int j = 0; j < i; j++) {
				int remainingPlayers;
				if (tourPlayersCount >= 2) {
					tourPlayersCount -= 2;
					remainingPlayers = 2;
				}
				else if (tourPlayersCount == 1) {
					tourPlayersCount -= 1;
					remainingPlayers = 1;
				}
				else {
					remainingPlayers = 0;
				}
				tourGames.add(new PlayOffGame(null, null, null, remainingPlayers));
			}
			games.add(tourGames);
			playersCount = playersCount % 2 == 0 ? playersCount / 2 : (playersCount + 1) / 2;
		}
		return Collections.unmodifiableList(games);
	}
}
