package com.github.tix320.jouska.core.game;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.game.creation.GameBoards;
import com.github.tix320.jouska.core.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.stock.Stock;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.util.collection.Tuple;

import static java.util.stream.Collectors.toMap;

public class SimpleGame implements Game {

	private static final int MAX_POINTS = 4;

	private final List<InGamePlayer> players;

	// ------------ Game State
	private final BoardCell[][] board;
	private final List<InGamePlayer> activePlayers;
	private final AtomicReference<InGamePlayer> currentPlayer;
	private final Stock<GameChange> changes;
	private final AtomicReference<GameState> gameState;
	private final AtomicReference<InGamePlayer> winner;
	private final List<InGamePlayer> lostPlayers;
	private final List<PlayerWithPoints> kickPlayers;
	private final Map<InGamePlayer, Integer> summaryStatistics;

	public static SimpleGame createPredefined(GameBoard board, List<InGamePlayer> players) {
		if (players.size() < 2) {
			throw new IllegalArgumentException("Players must be >=2");
		}
		return new SimpleGame(board, players);
	}

	public static SimpleGame createRandom(GameSettings gameSettings, Set<Player> players) {
		if (players.size() < 2) {
			throw new IllegalArgumentException("Players must be >=2");
		}
		if (gameSettings.getPlayersCount() != players.size()) {
			throw new IllegalStateException();
		}

		Tuple<List<InGamePlayer>, GameBoard> tuple = prepare(gameSettings, players);
		GameBoard board = tuple.second();
		List<InGamePlayer> gamePLayers = tuple.first();

		return new SimpleGame(board, gamePLayers);
	}

	private SimpleGame(GameBoard board, List<InGamePlayer> players) {
		this.changes = Stock.forObject();
		this.gameState = new AtomicReference<>(GameState.INITIAL);
		this.summaryStatistics = new HashMap<>();
		this.lostPlayers = new ArrayList<>();
		this.winner = new AtomicReference<>();
		this.kickPlayers = new ArrayList<>();
		this.players = List.copyOf(players);
		this.activePlayers = new ArrayList<>(players);
		this.currentPlayer = new AtomicReference<>(players.get(0));
		this.board = board.getMatrix();
	}

	private static Tuple<List<InGamePlayer>, GameBoard> prepare(GameSettings gameSettings, Set<Player> players) {
		int playersCount = gameSettings.getPlayersCount();

		PlayerColor[] colors = PlayerColor.getRandomPlayers(playersCount);

		List<Player> playersList = players.stream().collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
			Collections.shuffle(list);
			return list;
		}));

		List<InGamePlayer> gamePlayers = new ArrayList<>(playersList.size());
		for (int i = 0; i < playersList.size(); i++) {
			Player player = playersList.get(i);
			PlayerColor playerColor = colors[i];
			gamePlayers.add(new InGamePlayer(player, playerColor));
		}

		GameBoard board = GameBoards.createByType(gameSettings.getBoardType(), Arrays.asList(colors));
		return new Tuple<>(gamePlayers, board);
	}

	@Override
	public synchronized void start() {
		if (gameState.get() != GameState.INITIAL) {
			throw new RuntimeException("Game Already started");
		}

		initBoard();
		gameState.set(GameState.STARTED);
	}

	public synchronized CellChange turn(Point point) {
		failIfNotInProgress();
		int i = point.i;
		int j = point.j;
		BoardCell boardCell = board[i][j];
		PlayerColor player = boardCell.getColor();
		InGamePlayer currentPlayer = this.currentPlayer.get();
		if (currentPlayer.getColor() != player) {
			throw new IllegalTurnActorException(
					String.format("Current player %s cannot turn on cell %s:%s, which belongs to player %s",
							currentPlayer, i, j, Objects.requireNonNullElse(player, "None")));
		}

		CellChange cellChange = turn(point, player);

		resolveNextPlayer();
		checkStatistics();
		boolean needComplete = checkPlayers();

		changes.add(new PlayerTurn(cellChange));

		if (needComplete) {
			completeGame();
		}

		return cellChange;
	}

	public BoardCell[][] getBoard() {
		return board;
	}

	public Observable<GameChange> changes() {
		return changes.asObservable();
	}

	@Override
	public synchronized List<Point> getPointsBelongedToPlayer(Player player) {
		failIfNotStarted();
		InGamePlayer gamePlayer = findGamePlayerByPlayer(player);

		PlayerColor playerColor = gamePlayer.getColor();
		List<Point> points = new ArrayList<>();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				if (board[i][j].getColor() == playerColor) {
					points.add(new Point(i, j));
				}
			}
		}

		return points;
	}

	@Override
	public List<InGamePlayer> getPlayers() {
		return players;
	}

	@Override
	public synchronized List<InGamePlayer> getActivePlayers() {
		return Collections.unmodifiableList(activePlayers);
	}

	public synchronized InGamePlayer getCurrentPlayer() {
		failIfCompleted();
		return currentPlayer.get();
	}

	public synchronized Optional<InGamePlayer> ownerOfPoint(Point point) {
		failIfNotStarted();
		PlayerColor color = board[point.i][point.j].getColor();
		if (color == null) {
			return Optional.empty();
		}
		return Optional.of(getPlayerByColor(color));
	}

	public synchronized Statistics getStatistics() {
		return () -> Collections.unmodifiableMap(summaryStatistics);
	}

	public synchronized List<InGamePlayer> getLostPlayers() {
		return Collections.unmodifiableList(lostPlayers);
	}

	@Override
	public synchronized Optional<InGamePlayer> getWinner() {
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
		InGamePlayer gamePlayer = findGamePlayerByPlayer(player);
		PlayerWithPoints playerWithPoints = new PlayerWithPoints(gamePlayer, pointsBelongedToPlayer);
		kickPlayers.add(playerWithPoints);
		lostPlayers.add(gamePlayer);
		changes.add(new PlayerKick(playerWithPoints));

		boolean needComplete = checkPlayers();

		if (needComplete) {
			completeGame();
		}

		return playerWithPoints;
	}

	@Override
	public synchronized void forceCompleteGame(Player winner) {
		failIfNotInProgress();
		InGamePlayer winnerPlayer = findGamePlayerByPlayer(winner);
		List<InGamePlayer> remainingPlayers = new ArrayList<>(activePlayers);

		remainingPlayers.remove(winnerPlayer);

		lostPlayers.addAll(remainingPlayers);
		this.winner.set(winnerPlayer);
	}

	@Override
	public MonoObservable<None> completed() {
		return changes.asObservable()
				.filter(change -> change instanceof GameComplete)
				.map(aBoolean -> None.SELF)
				.toMono();
	}

	@Override
	public synchronized boolean isStarted() {
		GameState state = gameState.get();
		return state == GameState.STARTED || state == GameState.COMPLETED;
	}

	@Override
	public synchronized boolean isCompleted() {
		return gameState.get() == GameState.COMPLETED;
	}

	private List<InGamePlayer> resolveActivePlayers() {
		List<InGamePlayer> activePLayers = new ArrayList<>(this.players);
		activePLayers.removeAll(this.lostPlayers);
		activePLayers.removeAll(
				this.kickPlayers.stream().map(PlayerWithPoints::getPlayer).collect(Collectors.toList()));
		return activePLayers;
	}

	private void initBoard() {
		Map<InGamePlayer, Integer> statistics = players.stream().collect(toMap(player -> player, player -> 0));
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				BoardCell boardCell = board[i][j];
				PlayerColor color = boardCell.getColor();
				board[i][j] = boardCell;
				if (color != null) {
					InGamePlayer player = getPlayerByColor(boardCell.getColor());
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
		InGamePlayer currentPlayer = this.currentPlayer.get();

		while (true) {
			InGamePlayer nextPlayer = getNextPlayerOf(currentPlayer);
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
		for (Entry<InGamePlayer, Integer> entry : summaryStatistics.entrySet()) {
			InGamePlayer player = entry.getKey();
			Integer points = entry.getValue();
			if (points == 0) {
				lostPlayers.add(player);
			}
		}
	}

	private boolean checkPlayers() {
		activePlayers.clear();
		activePlayers.addAll(resolveActivePlayers());

		if (activePlayers.size() == 0) {
			throw new IllegalStateException();
		}

		return activePlayers.size() == 1;
	}

	private InGamePlayer getNextPlayerOf(InGamePlayer player) {
		int index = players.indexOf(player);
		if (index == players.size() - 1) {
			return players.get(0);
		}
		else {
			return players.get(index + 1);
		}
	}

	private void completeGame() {
		InGamePlayer winner = activePlayers.get(0);
		this.winner.set(winner);
		changes.add(new GameComplete(winner, lostPlayers));
		gameState.set(GameState.COMPLETED);
		changes.close();
	}

	private InGamePlayer getPlayerByColor(PlayerColor playerColor) {
		return players.stream()
				.filter(inGamePlayer -> inGamePlayer.getColor().equals(playerColor))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Color %s does not exists in this game", playerColor)));
	}

	private int getPointsOf(Point point) {
		int i = point.i;
		int j = point.j;
		BoardCell boardCell = board[i][j];
		return boardCell.getPoints();
	}

	private void putInfoToPoint(Point point, BoardCell boardCell) {
		BoardCell existBoardCell = board[point.i][point.j];
		board[point.i][point.j] = boardCell;

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
		if (i + 1 < board.length) {
			points.add(new Point(i + 1, j));
		}
		if (j - 1 >= 0) {
			points.add(new Point(i, j - 1));
		}
		if (j + 1 < board[0].length) {
			points.add(new Point(i, j + 1));
		}
		return points;
	}

	private void failIfNotStarted() {
		GameState state = gameState.get();
		if (state == GameState.INITIAL) {
			throw new IllegalStateException("Game does not started");
		}
	}

	private void failIfCompleted() {
		if (gameState.get() == GameState.COMPLETED) {
			throw new IllegalStateException("Game already completed");
		}
	}

	private void failIfNotInProgress() {
		if (gameState.get() != GameState.STARTED) {
			throw new IllegalStateException("Game does not started or completed");
		}
	}

	private InGamePlayer findGamePlayerByPlayer(Player player) {
		return getPlayers().stream()
				.filter(inGamePlayer -> inGamePlayer.getRealPlayer().equals(player))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Player %s does not participating in this game", player)));
	}

	private static class PointWithCellChange {
		public final Point point;
		public final CellChange cellChange;

		public PointWithCellChange(Point point, CellChange cellChange) {
			this.point = point;
			this.cellChange = cellChange;
		}
	}
}
