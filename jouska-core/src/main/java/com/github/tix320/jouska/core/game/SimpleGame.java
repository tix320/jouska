package com.github.tix320.jouska.core.game;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.github.tix320.jouska.core.game.proxy.CompletedInterceptor;
import com.github.tix320.jouska.core.game.proxy.StartedInterceptor;
import com.github.tix320.jouska.core.game.proxy.ThrowIfCompleted;
import com.github.tix320.jouska.core.game.proxy.ThrowIfNotStarted;
import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.proxy.AnnotationBasedProxyCreator;
import com.github.tix320.kiwi.api.proxy.ProxyCreator;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

public class SimpleGame implements Game {

	private static final ProxyCreator<SimpleGame> PROXY = new AnnotationBasedProxyCreator<>(SimpleGame.class,
			List.of(new StartedInterceptor(), new CompletedInterceptor()));

	private static final int MAX_POINTS = 4;

	private final GameSettings settings;

	private final CellInfo[][] board;

	private final List<InGamePlayer> players;

	private InGamePlayer currentPlayer;

	private final Stock<CellChange> turns;
	private final Property<Map<InGamePlayer, Integer>> summaryStatistics;
	private final Stock<InGamePlayer> lostPlayers;
	private final Stock<PlayerWithPoints> kickPlayers;
	private final Property<List<InGamePlayer>> completed;

	private final Lock lock;

	private boolean isStarted;
	// private void checkInstantiationSource() {
	// 	STACK_WALKER.walk(stackFrameStream -> stackFrameStream.filter(
	// 			stackFrame -> stackFrame.getDeclaringClass() == TestClass.class && stackFrame.getMethodName()
	// 					.equals("create"))
	// 			.findFirst()
	// 			.orElseThrow(() -> new IllegalStateException("Use factory method")));
	// }

	public static SimpleGame create(GameSettings gameSettings, GameBoard board, List<InGamePlayer> players) {
		return PROXY.create(gameSettings, board, players);
	}

	public SimpleGame(GameSettings settings, GameBoard board, List<InGamePlayer> players) {
		this.settings = settings;
		Map<InGamePlayer, Integer> playerSummaryPoints = new HashMap<>();
		for (InGamePlayer player : players) {
			playerSummaryPoints.put(player, 0);
		}

		turns = Stock.forObject();
		summaryStatistics = Property.forMap(playerSummaryPoints);
		lostPlayers = Stock.forObject();
		kickPlayers = Stock.forObject();
		completed = Property.forList();

		this.players = new ArrayList<>(players);
		this.currentPlayer = players.get(0);
		this.board = board.getMatrix();

		this.lock = new ReentrantLock();
	}

	@ThrowIfCompleted
	@Override
	public void start() {
		if (isStarted) {
			throw new RuntimeException("Already started");
		}
		runInLock(() -> {
			if (isStarted) {
				throw new RuntimeException("Already started");
			}
			isStarted = true;
			initBoard();
		});
	}

	@ThrowIfNotStarted
	@ThrowIfCompleted
	public void turn(Point point) {
		runInLock(() -> {
			int i = point.i;
			int j = point.j;
			CellInfo cellInfo = board[i][j];
			PlayerColor player = cellInfo.getPlayer();
			InGamePlayer currentPlayer = getCurrentPlayer();
			if (currentPlayer.getColor() != player) {
				throw new IllegalStateException(
						String.format("Current player %s cannot turn on cell %s:%s, which belongs to player %s",
								currentPlayer, i, j, Objects.requireNonNullElse(player, "None")));
			}


			CellChange cellChange = turn(point, player);
			this.currentPlayer = nextPlayer();
			turns.add(cellChange);
			checkLoses();
			checkWinner();
		});
	}

	@Override
	public GameSettings getSettings() {
		return settings;
	}

	public CellInfo[][] getBoard() {
		return board;
	}

	public ReadOnlyStock<CellChange> turns() {
		return turns.toReadOnly();
	}

	@ThrowIfNotStarted
	@Override
	public List<Point> getPointsBelongedToPlayer(Player player) {
		PlayerColor playerColor = findGamePlayerByPlayer(player).getColor();
		return runInLock(() -> {
			List<Point> points = new ArrayList<>();
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					if (board[i][j].getPlayer() == playerColor) {
						points.add(new Point(i, j));
					}
				}
			}

			return points;
		});
	}

	@Override
	public List<InGamePlayer> getPlayers() {
		return players;
	}

	@ThrowIfCompleted
	public InGamePlayer getCurrentPlayer() {
		return currentPlayer;
	}

	@ThrowIfNotStarted
	public InGamePlayer ownerOfPoint(Point point) {
		return runInLock(() -> {
			PlayerColor playerColor = board[point.i][point.j].getPlayer();

			return getPlayerByColor(playerColor);
		});
	}

	public Statistics getStatistics() {
		return summaryStatistics::toReadOnly;
	}

	public ReadOnlyStock<InGamePlayer> lostPlayers() {
		return lostPlayers.toReadOnly();
	}

	@Override
	public ReadOnlyStock<PlayerWithPoints> kickedPlayers() {
		return kickPlayers.toReadOnly();
	}

	@ThrowIfNotStarted
	@Override
	public void kick(Player player) {
		runInLock(() -> {
			List<Point> pointsBelongedToPlayer = getPointsBelongedToPlayer(player);
			for (Point point : pointsBelongedToPlayer) {
				putInfoToPoint(point, new CellInfo(null, 0));
			}
			InGamePlayer gamePlayer = findGamePlayerByPlayer(player);
			lostPlayers.add(gamePlayer);
			kickPlayers.add(new PlayerWithPoints(gamePlayer, pointsBelongedToPlayer));
			checkWinner();
		});
	}

	@ThrowIfNotStarted
	@ThrowIfCompleted
	@Override
	public void forceCompleteGame(Player winner) {
		runInLock(() -> {
			InGamePlayer winnerPlayer = findGamePlayerByPlayer(winner);
			List<InGamePlayer> remainingPlayers = getRemainingPlayers();

			remainingPlayers.remove(winnerPlayer);
			lostPlayers.addAll(remainingPlayers);

			List<InGamePlayer> lastView = new ArrayList<>(remainingPlayers);
			lastView.add(winnerPlayer);

			completed.set(lastView);
		});
	}

	@Override
	public MonoObservable<List<InGamePlayer>> onComplete() {
		return completed.asObservable().toMono();
	}

	@Override
	public Lock getLock() {
		return lock;
	}

	private void initBoard() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				CellInfo cellInfo = board[i][j];
				PlayerColor player = cellInfo.getPlayer();
				if (player != null) {
					changePointsForPlayer(player, cellInfo.getPoints());
				}
				board[i][j] = cellInfo;
			}
		}
	}

	private CellChange turn(Point rootPoint, PlayerColor player) {
		int rootNextPoint = Math.min(MAX_POINTS, getPointsOf(rootPoint) + 1);
		if (rootNextPoint < MAX_POINTS) {
			CellInfo cellInfo = new CellInfo(player, rootNextPoint);
			putInfoToPoint(rootPoint, cellInfo);
			return new CellChange(rootPoint, cellInfo, false, Collections.emptyList());
		}
		else {
			Set<Point> waitingCollapses = new HashSet<>();
			CellInfo rootCollapsingCellInfo = new CellInfo(null, 0);
			putInfoToPoint(rootPoint, rootCollapsingCellInfo);

			final CellChange rootFulling = new CellChange(rootPoint, new CellInfo(player, rootNextPoint), true,
					new ArrayList<>());
			final CellChange rootCollapsing = new CellChange(rootPoint, rootCollapsingCellInfo, false,
					new ArrayList<>());
			rootFulling.children.add(rootCollapsing);

			Queue<PointWithCellChange> pointsQueue = new LinkedList<>();

			List<Point> childPoints = findNeighbors(rootPoint);

			for (Point point : childPoints) {
				int points = getPointsOf(point);
				int nextPoint = Math.min(MAX_POINTS, points + 1);
				CellInfo cellInfo = new CellInfo(player, nextPoint);
				putInfoToPoint(point, cellInfo);
				CellChange cellChange = new CellChange(point, cellInfo, nextPoint == MAX_POINTS, new ArrayList<>());
				rootCollapsing.children.add(cellChange);
				if (nextPoint == MAX_POINTS) {
					waitingCollapses.add(point);
					pointsQueue.add(new PointWithCellChange(point, cellChange));
				}
			}

			while (!pointsQueue.isEmpty()) {
				PointWithCellChange pointWithCellChange = pointsQueue.remove();
				waitingCollapses.remove(pointWithCellChange.point);

				CellInfo collapsingCellInfo = new CellInfo(null, 0);
				putInfoToPoint(pointWithCellChange.point, collapsingCellInfo);

				final CellChange collapsing = new CellChange(pointWithCellChange.point, collapsingCellInfo, false,
						new ArrayList<>());
				pointWithCellChange.cellChange.children.add(collapsing);

				List<Point> childrenPoints = findNeighbors(pointWithCellChange.point);
				for (Point childPoint : childrenPoints) {
					int points = getPointsOf(childPoint);
					int nextPoint = Math.min(MAX_POINTS, points + 1);
					CellInfo childCellInfo = new CellInfo(player, nextPoint);
					putInfoToPoint(childPoint, childCellInfo);
					CellChange cellChange = new CellChange(childPoint, childCellInfo, nextPoint == MAX_POINTS,
							new ArrayList<>());
					collapsing.children.add(cellChange);
					if (nextPoint == MAX_POINTS && !waitingCollapses.contains(childPoint)) {
						waitingCollapses.add(childPoint);
						pointsQueue.add(new PointWithCellChange(childPoint, cellChange));
					}
				}
			}


			return rootFulling;
		}
	}

	private InGamePlayer getPlayerByColor(PlayerColor playerColor) {
		return players.stream()
				.filter(inGamePlayer -> inGamePlayer.getColor() == playerColor)
				.findFirst()
				.orElseThrow(IllegalStateException::new);
	}

	private int getPointsOf(Point point) {
		int i = point.i;
		int j = point.j;
		CellInfo cellInfo = board[i][j];
		return cellInfo.getPoints();
	}

	private void putInfoToPoint(Point point, CellInfo cellInfo) {
		CellInfo existCellInfo = board[point.i][point.j];
		changePointsForPlayer(existCellInfo.getPlayer(), -existCellInfo.getPoints());
		changePointsForPlayer(cellInfo.getPlayer(), cellInfo.getPoints());

		board[point.i][point.j] = cellInfo;
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

	private void changePointsForPlayer(PlayerColor playerColor, int points) {
		if (playerColor != null) {
			InGamePlayer player = getPlayerByColor(playerColor);
			Map<InGamePlayer, Integer> playerSummaryStatistics = summaryStatistics.get();
			playerSummaryStatistics.compute(player, (inGamePlayer, currentPoints) -> {
				if (currentPoints == null) {
					throw new IllegalStateException();
				}
				return currentPoints + points;
			});
			summaryStatistics.set(playerSummaryStatistics);
		}
	}

	private void checkLoses() {
		Iterator<Entry<InGamePlayer, Integer>> iterator = summaryStatistics.get().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<InGamePlayer, Integer> entry = iterator.next();
			InGamePlayer player = entry.getKey();
			Integer points = entry.getValue();
			if (points == 0) {
				iterator.remove();
				if (currentPlayer == player) {
					this.currentPlayer = previousPlayer();
				}
				lostPlayers.add(player);
			}
		}
	}

	private void checkWinner() {
		if (lostPlayers.list().size() == this.players.size() - 1) { // 1 player left
			List<InGamePlayer> leftPlayers = new ArrayList<>(this.players);
			leftPlayers.removeAll(lostPlayers.list());
			InGamePlayer gamePlayer = leftPlayers.get(0);
			forceCompleteGame(gamePlayer.getPlayer());
		}
	}

	private InGamePlayer nextPlayer() {
		int currentPlayerIndex = players.indexOf(this.currentPlayer);
		if (currentPlayerIndex == players.size() - 1) {
			return players.get(0);
		}
		else {
			return players.get(currentPlayerIndex + 1);
		}
	}

	private InGamePlayer previousPlayer() {
		int currentPlayerIndex = players.indexOf(this.currentPlayer);
		if (currentPlayerIndex == 0) {
			return players.get(players.size() - 1);
		}
		else {
			return players.get(currentPlayerIndex - 1);
		}
	}

	private List<InGamePlayer> getRemainingPlayers() {
		List<InGamePlayer> remainingPlayers = new ArrayList<>(this.players);
		remainingPlayers.removeAll(this.lostPlayers.list());
		return remainingPlayers;
	}

	@Override
	public boolean isStarted() {
		return runInLock(() -> isStarted);
	}

	@Override
	public boolean isCompleted() {
		return runInLock(() -> completed.get() != null);
	}

	private void runInLock(Runnable runnable) {
		try {
			lock.lock();
			runnable.run();
		}
		finally {
			lock.unlock();
		}
	}

	private <T> T runInLock(Supplier<T> supplier) {
		try {
			lock.lock();
			return supplier.get();
		}
		finally {
			lock.unlock();
		}
	}

	private InGamePlayer findGamePlayerByPlayer(Player player) {
		return getPlayers().stream()
				.filter(inGamePlayer -> inGamePlayer.getPlayer() == player)
				.findFirst()
				.orElseThrow();
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
