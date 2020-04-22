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
import com.github.tix320.kiwi.api.util.None;

public class ClassicPlayOff implements RestorablePlayOff {

	private final ClassicPlayOffSettings settings;

	private final List<Player> players;

	private final Property<List<List<PlayOffGame>>> tours;

	private final Property<PlayOffState> state;

	private final AtomicReference<Player> winner;

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
		this.tours = Property.forObject(initGamesSpace(players.size()));
		this.winner = new AtomicReference<>();
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
	public Observable<List<List<PlayOffGame>>> getTours() {
		return tours.asObservable()
				.map(tours -> tours.stream()
						.map(List::copyOf)
						.collect(Collectors.toUnmodifiableList())); // return deep immutable copy
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
	public synchronized void restore(List<List<PlayOffGame>> structure) throws RestoreException {
		if (state.getValue() != PlayOffState.INITIAL) {
			throw new RestoreException("Play-off already started");
		}

		try {
			List<List<PlayOffGame>> tours = this.tours.getValue();
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
					PlayOffGame playOffGame = tour.get(j);
					Game game = playOffGame.getGame();
					if (game != null && !game.isCompleted()) {
						int tourIndex = i;
						int tourGameIndex = j;
						game.completed().subscribe(g -> onGameComplete(tourIndex, tourGameIndex, game));
					}
				}
			}
			state.setValue(PlayOffState.RUNNING);

			Game lastGame = tours.get(tours.size() - 1).get(0).getGame();
			if (lastGame != null && lastGame.isCompleted()) {
				Property.inAtomicContext(() -> {
					winner.set(lastGame.getWinner().orElseThrow().getRealPlayer());
					state.setValue(PlayOffState.COMPLETED);
				});
			}
		}
		catch (RuntimeException e) {
			throw new RestoreException("Failed to restore.", e);
		}

	}

	private void fillFirstTour() {
		Iterator<Player> playersIterator = players.iterator();

		List<List<PlayOffGame>> tours = this.tours.getValue();
		List<PlayOffGame> firstTourGames = tours.get(0);

		if (players.size() % 2 == 0) {
			for (int i = 0; i < firstTourGames.size(); i++) {
				Player firstPlayer = playersIterator.next();
				Player secondPlayer = playersIterator.next();
				PlayOffGame playOffGame = createNewGame(firstPlayer, secondPlayer);
				registerGame(playOffGame, 0, i);
			}
		}
		else {
			for (int i = 0; i < firstTourGames.size() - 1; i++) {
				Player firstPlayer = playersIterator.next();
				Player secondPlayer = playersIterator.next();

				PlayOffGame playOffGame = createNewGame(firstPlayer, secondPlayer);
				registerGame(playOffGame, 0, i);
			}

			int lastGameIndex = firstTourGames.size() - 1;
			Player singlePlayer = playersIterator.next();
			PlayOffGame playOffGame = createNewGame(singlePlayer, null);
			registerGame(playOffGame, 0, lastGameIndex);
		}
	}

	private void onGameComplete(int tourIndex, int tourGameIndex, Game game) {
		Player winnerOfPreviousGame = game.getWinner().orElseThrow().getRealPlayer();

		List<List<PlayOffGame>> tours = this.tours.getValue();
		int toursCount = tours.size();
		if (tourIndex == toursCount - 1) { // last tour, last game
			Property.inAtomicContext(() -> {
				state.setValue(PlayOffState.COMPLETED);
				winner.set(winnerOfPreviousGame);
			});
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
	}

	private PlayOffGame createNewGame(Player firstPlayer, Player secondPlayer) {
		Objects.requireNonNull(firstPlayer);
		GameSettings baseGameSettings = this.settings.getBaseGameSettings();
		PlayOffGame playOffGame;
		if (secondPlayer == null) {
			GameSettings gameSettings = baseGameSettings.changeName(String.format("%s VS <None>", firstPlayer))
					.changePlayersCount(1);
			Game game = gameSettings.createGame();
			game.addPlayer(new GamePlayer(firstPlayer, PlayerColor.RED));
			game.start();
			game.forceCompleteGame(firstPlayer);
			playOffGame = new PlayOffGame(firstPlayer, null, game);
		}
		else {
			GameSettings gameSettings = baseGameSettings.changeName(
					String.format("%s VS %s", firstPlayer, secondPlayer));
			Game game = gameSettings.createGame();
			game.addPlayer(new GamePlayer(firstPlayer, PlayerColor.RED));
			game.addPlayer(new GamePlayer(secondPlayer, PlayerColor.BLUE));
			game.shufflePLayers();
			playOffGame = new PlayOffGame(firstPlayer, secondPlayer, game);
		}

		return playOffGame;
	}

	private void registerGame(PlayOffGame playOffGame, int tourIndex, int tourGameIndex) {
		tours.getValue().get(tourIndex).set(tourGameIndex, playOffGame);

		Game game = playOffGame.getGame();
		game.completed().subscribe(gameO -> onGameComplete(tourIndex, tourGameIndex, gameO));
	}

	private void failIfStarted() {
		PlayOffState state = this.state.getValue();
		if (state != PlayOffState.INITIAL) {
			throw new TournamentIllegalStateException("Play-off already started");
		}
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
