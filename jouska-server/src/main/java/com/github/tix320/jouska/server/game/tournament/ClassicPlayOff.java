package com.github.tix320.jouska.server.game.tournament;

import java.util.*;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.creation.GameFactory;
import com.github.tix320.jouska.core.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.MathUtils;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.Property;

import static java.util.stream.Collectors.toList;

public class ClassicPlayOff implements PlayOff {

	private final GameSettings gameSettings;

	private final List<Player> players;

	private final Property<List<List<GameSpace>>> games;

	private final Property<Boolean> completed;

	public ClassicPlayOff(GameSettings gameSettings, List<Player> players) {
		if (players.size() < 2) {
			throw new IllegalArgumentException("Players count must be >=2");
		}

		int playersCount = gameSettings.getPlayersCount();
		if (playersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic play off game must be for two players",
							playersCount));
		}

		this.gameSettings = gameSettings;
		this.completed = Property.forObject(false);
		this.players = Collections.unmodifiableList(players);
		int toursCount = resolveNumberOfTours(players.size());
		this.games = Property.forObject(initGamesSpace(toursCount));
		fillFirstTour();
	}

	@Override
	public Observable<List<List<Game>>> games() {
		return games.asObservable()
				.map(gameSpaces -> gameSpaces.stream()
						.map(gameSpaces1 -> gameSpaces1.stream().map(GameSpace::getGame).collect(toList()))
						.collect(toList()));
	}

	@Override
	public MonoObservable<Boolean> completed() {
		return completed.asObservable().skip(1).toMono();
	}

	private void fillFirstTour() {
		List<Player> players = new ArrayList<>(this.players);
		Collections.shuffle(players);

		Iterator<Player> playersIterator = players.iterator();

		List<GameSpace> tourGames = this.games.getValue().get(0);
		for (int i = 0; i < tourGames.size(); i++) {
			GameSpace gameSpace;
			Player firstPlayer = playersIterator.next();
			if (playersIterator.hasNext()) {
				Player secondPlayer = playersIterator.next();
				Game game = GameFactory.create(gameSettings, Set.of(firstPlayer, secondPlayer));
				gameSpace = new GameSpace();
				gameSpace.game = game;
				gameSpace.addPlayer(firstPlayer);
				gameSpace.addPlayer(secondPlayer);
			}
			else {
				Game game = GameFactory.create(gameSettings, Set.of(firstPlayer));
				game.start();
				game.forceCompleteGame(firstPlayer);
				gameSpace = new GameSpace();
				gameSpace.game = game;
				gameSpace.firstPlayer = firstPlayer;
			}
			final Game game = gameSpace.game;
			final int tourGameIndex = i;
			game.completed().subscribe(ignored -> onGameComplete(0, tourGameIndex, game));

			tourGames.set(i, gameSpace);
		}
	}

	private void onGameComplete(int tourIndex, int tourGameIndex, Game game) {
		Player winner = game.getWinner().get().getRealPlayer();

		List<List<GameSpace>> gameSpaces = games.getValue();
		int toursCount = gameSpaces.size();
		if (tourIndex == toursCount) {
			completed.setValue(true);
		}
		else {
			List<GameSpace> nextTourGames = gameSpaces.get(tourIndex + 1);
			int currentPLayerSpaceInNextTour = (int) MathUtils.log2(MathUtils.nextPowerOf2(tourGameIndex));
			GameSpace gameSpace;
			if (nextTourGames.get(currentPLayerSpaceInNextTour) == null) {
				gameSpace = new GameSpace();
				gameSpace.addPlayer(winner);
				nextTourGames.set(currentPLayerSpaceInNextTour, gameSpace);
			}
			else {
				gameSpace = nextTourGames.get(currentPLayerSpaceInNextTour);
				gameSpace.addPlayer(winner);
			}

			if (gameSpace.isReady()) {
				Game newGame = GameFactory.create(gameSettings, Set.of(gameSpace.firstPlayer, gameSpace.secondPlayer));
				newGame.completed()
						.subscribe(ignored -> onGameComplete(tourIndex + 1, currentPLayerSpaceInNextTour, newGame));
				gameSpace.game = newGame;
			}
			nextTourGames.set(currentPLayerSpaceInNextTour, gameSpace);
			games.setValue(games.getValue());
		}
	}

	private static List<List<GameSpace>> initGamesSpace(int toursCount) {
		List<List<GameSpace>> games = new ArrayList<>();
		for (int i = 0; i < toursCount; i++) {
			int tourGamesCount = ((int) Math.pow(2, toursCount - i)) / 2;
			List<GameSpace> tourGameSpaces = new ArrayList<>(tourGamesCount);
			for (int j = 0; j < tourGamesCount; j++) {
				tourGameSpaces.add(null);
			}
			games.add(Collections.unmodifiableList(tourGameSpaces));
		}
		return Collections.unmodifiableList(games);
	}

	private static int resolveNumberOfTours(int playersCount) {
		int toursCount = (int) MathUtils.log2(playersCount);
		if (!MathUtils.isPowerOfTwo(toursCount)) {
			return (int) Math.ceil(toursCount);
		}
		return toursCount;
	}

	private static final class GameSpace {
		private Player firstPlayer;
		private Player secondPlayer;
		private Game game;

		public void addPlayer(Player player) {
			if (firstPlayer == null) {
				firstPlayer = player;
			}
			else if (secondPlayer == null) {
				secondPlayer = player;
			}
			else {
				throw new IllegalStateException();
			}
		}

		public Game getGame() {
			return game;
		}

		boolean isReady() {
			return firstPlayer != null && secondPlayer != null;
		}
	}
}
