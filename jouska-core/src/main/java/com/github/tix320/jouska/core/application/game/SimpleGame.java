package com.github.tix320.jouska.core.application.game;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.creation.GameBoards;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.application.game.creation.SimpleGameSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;
import com.github.tix320.jouska.core.infrastructure.UnsupportedChangeException;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.property.Stock;

import static java.util.stream.Collectors.toMap;

public final class SimpleGame implements RestorableGame {

	private static final int MAX_POINTS = 4;

	private final SimpleGameSettings settings;
	private final List<GamePlayer> players;

	// ------------ Game State
	private GameBoard board; // init after start
	private final List<GamePlayer> activePlayers;
	private final AtomicReference<GamePlayer> currentPlayer;
	private final Stock<GameChange> changes;
	private final Property<GameState> gameState;
	private final AtomicReference<GamePlayer> winner;
	private final List<GamePlayer> lostPlayers;
	private final List<PlayerWithPoints> kickPlayers;
	private final Map<GamePlayer, Integer> summaryStatistics;

	public static SimpleGame create(SimpleGameSettings gameSettings) {
		return new SimpleGame(gameSettings);
	}

	private SimpleGame(SimpleGameSettings settings) {
		this.settings = settings;
		this.changes = Stock.forObject();
		this.gameState = Property.forObject(GameState.INITIAL);
		this.summaryStatistics = new HashMap<>();
		this.lostPlayers = new ArrayList<>();
		this.winner = new AtomicReference<>();
		this.kickPlayers = new ArrayList<>();
		this.players = new ArrayList<>();
		this.activePlayers = new ArrayList<>();
		this.currentPlayer = new AtomicReference<>();
	}

	@Override
	public RestorableGameSettings getSettings() {
		return settings;
	}

	@Override
	public synchronized void addPlayer(GamePlayer player) {
		failIfStarted();

		int playersCount = settings.getPlayersCount();
		if (playersCount == players.size()) {
			throw new GameAlreadyFullException(String.format("Already full. Count %s", playersCount));
		}

		if (getPlayers().contains(player.getRealPlayer())) {
			throw new IllegalArgumentException("Player already added");
		}

		players.add(player);
	}

	@Override
	public synchronized boolean removePlayer(Player player) {
		failIfStarted();

		return players.removeIf(gamePlayer -> gamePlayer.getRealPlayer().equals(player));
	}

	@Override
	public synchronized void shufflePLayers() {
		failIfStarted();

		int playersCount = settings.getPlayersCount();

		PlayerColor[] colors = PlayerColor.random(playersCount);

		List<GamePlayer> gamePlayers = new ArrayList<>(players.size());
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i).getRealPlayer();
			PlayerColor playerColor = colors[i];
			gamePlayers.add(new GamePlayer(player, playerColor));
		}

		players.clear();
		players.addAll(gamePlayers);
	}

	@Override
	public synchronized void start() {
		failIfStarted();

		int playersCount = settings.getPlayersCount();
		if (playersCount != players.size()) {
			throw new GameIllegalStateException(String.format("Game players not fully. Count %s", playersCount));
		}

		Set<PlayerColor> uniqueColors = players.stream().map(GamePlayer::getColor).collect(Collectors.toSet());
		if (uniqueColors.size() != players.size()) {
			throw new GameIllegalStateException("Not unique colors");
		}

		this.board = GameBoards.createByType(settings.getBoardType(),
				players.stream().map(GamePlayer::getColor).collect(Collectors.toList()));

		this.activePlayers.addAll(players);
		this.currentPlayer.set(players.get(0));

		calculateInitialStatistics();

		gameState.setValue(GameState.RUNNING);
	}

	@Override
	public synchronized void restore(List<GameChange> changes) throws UnsupportedChangeException {
		if (gameState.getValue() != GameState.INITIAL) {
			throw new RestoreException("Game already started");
		}

		ChangeVisitor changeVisitor = new ChangeVisitor();
		for (GameChange change : changes) {
			if (isCompleted()) {
				throw new IllegalStateException(
						"Game completed based on changes. but still remained changes, which was not applied");
			}
			change.accept(changeVisitor);
		}
	}

	@Override
	public synchronized CellChange turn(Point point) {
		failIfNotInProgress();
		int i = point.i;
		int j = point.j;
		BoardCell boardCell = board.get(i, j);
		PlayerColor player = boardCell.getColor();
		GamePlayer currentPlayer = this.currentPlayer.get();
		if (currentPlayer.getColor() != player) {
			throw new IllegalTurnActorException(
					String.format("Current player %s cannot turn on cell %s:%s, which belongs to player %s",
							currentPlayer, i, j, Objects.requireNonNullElse(player, "None")));
		}

		CellChange cellChange = turn(point, player);

		resolveNextPlayer();
		checkStatistics();
		boolean needComplete = activePlayers.size() == 1;

		changes.add(new PlayerTurn(point));

		if (needComplete) {
			completeGame(activePlayers.get(0));
		}

		return cellChange;
	}

	public ReadOnlyStock<GameChange> changes() {
		return changes.toReadOnly();
	}

	@Override
	public synchronized List<Point> getPointsBelongedToPlayer(Player player) {
		GamePlayer gamePlayer = findGamePlayerByPlayer(player);

		PlayerColor playerColor = gamePlayer.getColor();
		List<Point> points = new ArrayList<>();
		GameBoard board = this.board;
		for (int i = 0; i < board.getHeight(); i++) {
			for (int j = 0; j < board.getWidth(); j++) {
				if (board.get(i, j).getColor() == playerColor) {
					points.add(new Point(i, j));
				}
			}
		}

		return points;
	}

	@Override
	public synchronized List<Player> getPlayers() {
		return players.stream().map(GamePlayer::getRealPlayer).collect(Collectors.toUnmodifiableList());
	}

	@Override
	public synchronized List<GamePlayer> getPlayersWithColors() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public synchronized List<GamePlayer> getActivePlayers() {
		return Collections.unmodifiableList(activePlayers);
	}

	public synchronized GamePlayer getCurrentPlayer() {
		failIfCompleted();
		return currentPlayer.get();
	}

	public synchronized Optional<GamePlayer> ownerOfPoint(Point point) {
		failIfNotStarted();
		PlayerColor color = board.get(point.i, point.j).getColor();
		if (color == null) {
			return Optional.empty();
		}
		return Optional.of(getPlayerByColor(color));
	}

	public synchronized Statistics getStatistics() {
		return () -> Collections.unmodifiableMap(summaryStatistics);
	}

	public synchronized List<GamePlayer> getLosers() {
		return Collections.unmodifiableList(lostPlayers);
	}

	@Override
	public synchronized Optional<GamePlayer> getWinner() {
		return Optional.ofNullable(winner.get());
	}

	@Override
	public synchronized List<PlayerWithPoints> getKickedPlayers() {
		return Collections.unmodifiableList(kickPlayers);
	}

	@Override
	public synchronized PlayerWithPoints kick(Player player) {
		failIfNotInProgress();
		List<Point> pointsBelongedToPlayer = getPointsBelongedToPlayer(player);
		for (Point point : pointsBelongedToPlayer) {
			putInfoToPoint(point, new BoardCell(null, 0));
		}
		GamePlayer gamePlayer = findGamePlayerByPlayer(player);
		PlayerWithPoints playerWithPoints = new PlayerWithPoints(gamePlayer, pointsBelongedToPlayer);
		kickPlayers.add(playerWithPoints);
		lostPlayers.add(gamePlayer);
		activePlayers.remove(gamePlayer);
		changes.add(new PlayerKick(playerWithPoints));

		boolean needComplete = activePlayers.size() == 1;

		if (needComplete) {
			completeGame(activePlayers.get(0));
		}

		return playerWithPoints;
	}

	@Override
	public synchronized void forceCompleteGame(Player winner) {
		failIfNotInProgress();
		GamePlayer winnerPlayer = findGamePlayerByPlayer(winner);
		List<GamePlayer> remainingPlayers = new ArrayList<>(activePlayers);

		remainingPlayers.remove(winnerPlayer);

		lostPlayers.addAll(remainingPlayers);
		completeGame(winnerPlayer);
	}

	@Override
	public synchronized GameState getState() {
		return gameState.getValue();
	}

	@Override
	public MonoObservable<? extends Game> completed() {
		return gameState.asObservable().filter(state -> state == GameState.COMPLETED).map(state -> this).toMono();
	}

	@Override
	public synchronized boolean isStarted() {
		GameState state = gameState.getValue();
		return state == GameState.RUNNING || state == GameState.COMPLETED;
	}

	@Override
	public synchronized boolean isCompleted() {
		return gameState.getValue() == GameState.COMPLETED;
	}

	private void calculateInitialStatistics() {
		Map<GamePlayer, Integer> statistics = players.stream().collect(toMap(player -> player, player -> 0));
		GameBoard board = this.board;
		for (int i = 0; i < board.getHeight(); i++) {
			for (int j = 0; j < board.getWidth(); j++) {
				BoardCell boardCell = board.get(i, j);
				PlayerColor color = boardCell.getColor();
				board.set(i, j, boardCell);
				if (color != null) {
					GamePlayer player = getPlayerByColor(boardCell.getColor());
					statistics.compute(player, (p, points) -> {
						if (points == null) {
							throw new IllegalStateException();
						}
						return points + boardCell.getPoints();
					});
				}
			}
		}

		summaryStatistics.putAll(statistics);
	}

	private CellChange turn(Point rootPoint, PlayerColor player) {
		int rootNextPoint = Math.min(MAX_POINTS, getPointsOf(rootPoint) + 1);
		if (rootNextPoint < MAX_POINTS) {
			BoardCell boardCell = new BoardCell(player, rootNextPoint);
			putInfoToPoint(rootPoint, boardCell);
			return new CellChange(rootPoint, boardCell, false, Collections.emptyList());
		}
		else {
			Set<Point> waitingCollapses = new HashSet<>();
			BoardCell rootCollapsingBoardCell = new BoardCell(null, 0);
			putInfoToPoint(rootPoint, rootCollapsingBoardCell);

			final CellChange rootFulling = new CellChange(rootPoint, new BoardCell(player, rootNextPoint), true,
					new ArrayList<>());
			final CellChange rootCollapsing = new CellChange(rootPoint, rootCollapsingBoardCell, false,
					new ArrayList<>());
			rootFulling.getChildren().add(rootCollapsing);

			Queue<PointWithCellChange> pointsQueue = new LinkedList<>();

			List<Point> childPoints = findNeighbors(rootPoint);

			for (Point point : childPoints) {
				int points = getPointsOf(point);
				int nextPoint = Math.min(MAX_POINTS, points + 1);
				BoardCell boardCell = new BoardCell(player, nextPoint);
				putInfoToPoint(point, boardCell);
				CellChange cellChange = new CellChange(point, boardCell, nextPoint == MAX_POINTS, new ArrayList<>());
				rootCollapsing.getChildren().add(cellChange);
				if (nextPoint == MAX_POINTS) {
					waitingCollapses.add(point);
					pointsQueue.add(new PointWithCellChange(point, cellChange));
				}
			}

			while (!pointsQueue.isEmpty()) {
				PointWithCellChange pointWithCellChange = pointsQueue.remove();
				waitingCollapses.remove(pointWithCellChange.point);

				BoardCell collapsingBoardCell = new BoardCell(null, 0);
				putInfoToPoint(pointWithCellChange.point, collapsingBoardCell);

				final CellChange collapsing = new CellChange(pointWithCellChange.point, collapsingBoardCell, false,
						new ArrayList<>());
				pointWithCellChange.cellChange.getChildren().add(collapsing);

				List<Point> childrenPoints = findNeighbors(pointWithCellChange.point);
				for (Point childPoint : childrenPoints) {
					int points = getPointsOf(childPoint);
					int nextPoint = Math.min(MAX_POINTS, points + 1);
					BoardCell childBoardCell = new BoardCell(player, nextPoint);
					putInfoToPoint(childPoint, childBoardCell);
					CellChange cellChange = new CellChange(childPoint, childBoardCell, nextPoint == MAX_POINTS,
							new ArrayList<>());
					collapsing.getChildren().add(cellChange);
					if (nextPoint == MAX_POINTS && !waitingCollapses.contains(childPoint)) {
						waitingCollapses.add(childPoint);
						pointsQueue.add(new PointWithCellChange(childPoint, cellChange));
					}
				}
			}


			return rootFulling;
		}
	}

	private void resolveNextPlayer() {
		GamePlayer currentPlayer = this.currentPlayer.get();

		while (true) {
			GamePlayer nextPlayer = getNextPlayerOf(currentPlayer);
			if (activePlayers.contains(nextPlayer)) {
				this.currentPlayer.set(nextPlayer);
				break;
			}
			else {
				currentPlayer = nextPlayer;
			}
		}
	}

	private void checkStatistics() {
		for (Entry<GamePlayer, Integer> entry : summaryStatistics.entrySet()) {
			GamePlayer player = entry.getKey();
			Integer points = entry.getValue();
			if (points == 0) {
				lostPlayers.add(player);
				activePlayers.remove(player);
			}
		}
	}

	private GamePlayer getNextPlayerOf(GamePlayer player) {
		int index = players.indexOf(player);
		if (index == players.size() - 1) {
			return players.get(0);
		}
		else {
			return players.get(index + 1);
		}
	}

	private void completeGame(GamePlayer winner) {
		this.winner.set(winner);
		changes.add(new GameComplete(winner, lostPlayers));
		changes.close();
		gameState.setValue(GameState.COMPLETED);
		gameState.close();
	}

	private GamePlayer getPlayerByColor(PlayerColor playerColor) {
		return players.stream()
				.filter(inGamePlayer -> inGamePlayer.getColor().equals(playerColor))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Color %s does not exists in this game", playerColor)));
	}

	private int getPointsOf(Point point) {
		int i = point.i;
		int j = point.j;
		BoardCell boardCell = board.get(i, j);
		return boardCell.getPoints();
	}

	private void putInfoToPoint(Point point, BoardCell boardCell) {
		BoardCell existBoardCell = board.get(point.i, point.j);
		board.set(point.i, point.j, boardCell);

		if (existBoardCell.getColor() != null) {
			changeColorStatistics(existBoardCell.getColor(), -existBoardCell.getPoints());
		}

		if (boardCell.getColor() != null) {
			changeColorStatistics(boardCell.getColor(), boardCell.getPoints());
		}
	}

	private void changeColorStatistics(PlayerColor color, int change) {
		summaryStatistics.compute(getPlayerByColor(color), (inGamePlayer, currentPoints) -> {
			if (currentPoints == null) {
				throw new IllegalStateException();
			}
			return currentPoints + change;
		});
	}

	private List<Point> findNeighbors(Point point) {
		int i = point.i;
		int j = point.j;
		List<Point> points = new ArrayList<>(4);
		if (i - 1 >= 0) {
			points.add(new Point(i - 1, j));
		}
		if (i + 1 < board.getHeight()) {
			points.add(new Point(i + 1, j));
		}
		if (j - 1 >= 0) {
			points.add(new Point(i, j - 1));
		}
		if (j + 1 < board.getWidth()) {
			points.add(new Point(i, j + 1));
		}
		return points;
	}

	private void failIfStarted() {
		GameState state = gameState.getValue();
		if (state != GameState.INITIAL) {
			throw new GameIllegalStateException("Game already started");
		}
	}

	private void failIfNotStarted() {
		GameState state = gameState.getValue();
		if (state == GameState.INITIAL) {
			throw new GameIllegalStateException("Game does not started");
		}
	}

	private void failIfCompleted() {
		if (gameState.getValue() == GameState.COMPLETED) {
			throw new GameIllegalStateException("Game already completed");
		}
	}

	private void failIfNotInProgress() {
		if (gameState.getValue() != GameState.RUNNING) {
			throw new GameIllegalStateException("Game does not started or completed");
		}
	}

	private GamePlayer findGamePlayerByPlayer(Player player) {
		return getPlayersWithColors().stream()
				.filter(inGamePlayer -> inGamePlayer.getRealPlayer().equals(player))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Player %s does not participating in this game", player)));
	}

	@Override
	public Object getLock() {
		return this;
	}

	private static class PointWithCellChange {
		public final Point point;
		public final CellChange cellChange;

		public PointWithCellChange(Point point, CellChange cellChange) {
			this.point = point;
			this.cellChange = cellChange;
		}
	}

	private final class ChangeVisitor implements GameChangeVisitor {

		@Override
		public void visit(PlayerTurn playerTurn) {
			turn(playerTurn.getPoint());
		}

		@Override
		public void visit(PlayerTimedTurn playerTimedTurn) {
			throw new UnsupportedChangeException(playerTimedTurn.getClass().toString());
		}

		@Override
		public void visit(PlayerKick playerKick) {
			kick(playerKick.getPlayerWithPoints().getPlayer().getRealPlayer());
		}

		@Override
		public void visit(GameComplete gameComplete) {

		}
	}
}
