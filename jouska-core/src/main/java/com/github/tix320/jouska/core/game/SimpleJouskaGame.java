package com.github.tix320.jouska.core.game;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.github.tix320.jouska.core.infastructure.CheckCompleted;
import com.github.tix320.jouska.core.infastructure.CheckStarted;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.proxy.AnnotationBasedProxyCreator;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.proxy.ProxyCreator;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.CachedPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher;
import com.github.tix320.kiwi.api.util.None;

public class SimpleJouskaGame implements JouskaGame {

	private static final ProxyCreator<SimpleJouskaGame> PROXY = new AnnotationBasedProxyCreator<>(
			SimpleJouskaGame.class, List.of(new AnnotationInterceptor<>(CheckStarted.class, (method, target) -> {
		if (!target.isStarted()) {
			throw new IllegalStateException("Game does not started");
		}
		return None.SELF;
	}), new AnnotationInterceptor<>(CheckCompleted.class, (method, target) -> {
		if (target.isCompleted()) {
			throw new IllegalStateException("Game already completed");
		}
		return None.SELF;
	})));

	private static final int MAX_POINTS = 4;

	private final CellInfo[][] board;

	private final List<Player> players;

	private final Map<Player, Integer> playerSummaryPoints;

	private Player currentPlayer;

	private final Publisher<CellChange> turns;
	private final Publisher<Map<Player, Integer>> summaryStatistics;
	private final CachedPublisher<Player> lostPlayers;
	private final CachedPublisher<PlayerWithPoints> kickPlayers;
	private final SinglePublisher<List<Player>> onComplete;

	private final Lock lock;

	private boolean isStarted;
	// private void checkInstantiationSource() {
	// 	STACK_WALKER.walk(stackFrameStream -> stackFrameStream.filter(
	// 			stackFrame -> stackFrame.getDeclaringClass() == TestClass.class && stackFrame.getMethodName()
	// 					.equals("create"))
	// 			.findFirst()
	// 			.orElseThrow(() -> new IllegalStateException("Use factory method")));
	// }

	public static JouskaGame create(GameBoard board, Player[] players) {
		return PROXY.create(board, players);
	}

	public SimpleJouskaGame(GameBoard board, Player[] players) {
		playerSummaryPoints = new EnumMap<>(Player.class);
		for (Player player : players) {
			playerSummaryPoints.put(player, 0);
		}

		turns = Publisher.cached();
		summaryStatistics = Publisher.single(Collections.unmodifiableMap(playerSummaryPoints));
		lostPlayers = Publisher.cached();
		kickPlayers = Publisher.cached();
		onComplete = Publisher.single();

		this.players = new ArrayList<>(Arrays.asList(players));
		this.currentPlayer = players[0];
		this.board = board.getMatrix();

		this.lock = new ReentrantLock();
	}

	@CheckCompleted
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

	@CheckStarted
	@CheckCompleted
	public void turn(Point point) {
		runInLock(() -> {
			int i = point.i;
			int j = point.j;
			CellInfo cellInfo = board[i][j];
			Player player = cellInfo.getPlayer();
			Player currentPlayer = getCurrentPlayer();
			if (currentPlayer != player) {
				throw new IllegalStateException(
						String.format("Current player %s cannot turn on cell %s:%s, which belongs to player %s",
								currentPlayer, i, j, Objects.requireNonNullElse(player, "None")));
			}


			CellChange cellChange = turn(point, player);
			this.currentPlayer = nextPlayer();
			turns.publish(cellChange);
			checkLoses();
			checkWinner();
		});
	}

	@CheckStarted
	public CellInfo[][] getBoard() {
		return board;
	}

	public Observable<CellChange> turns() {
		return turns.asObservable();
	}

	@CheckStarted
	public List<Point> getPointsBelongedToPlayer(Player player) {
		return runInLock(() -> {
			List<Point> points = new ArrayList<>();
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board[i].length; j++) {
					if (board[i][j].getPlayer() == player) {
						points.add(new Point(i, j));
					}
				}
			}

			return points;
		});
	}

	@CheckCompleted
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	@CheckStarted
	public Player ownerOfPoint(Point point) {
		return runInLock(() -> board[point.i][point.j].getPlayer());
	}

	public Statistics getStatistics() {
		return new Statistics() {
			@Override
			public Observable<Map<Player, Integer>> summaryPoints() {
				return summaryStatistics.asObservable();
			}
		};
	}

	public Observable<Player> lostPlayers() {
		return lostPlayers.asObservable();
	}

	@Override
	public Observable<PlayerWithPoints> kickedPlayers() {
		return kickPlayers.asObservable();
	}

	@CheckStarted
	@Override
	public void kick(Player player) {
		runInLock(() -> {
			List<Point> pointsBelongedToPlayer = getPointsBelongedToPlayer(player);
			for (Point point : pointsBelongedToPlayer) {
				putInfoToPoint(point, new CellInfo(null, 0));
			}
			lostPlayers.publish(player);
			kickPlayers.publish(new PlayerWithPoints(player, pointsBelongedToPlayer));
			checkWinner();
		});
	}

	@CheckStarted
	@CheckCompleted
	@Override
	public void forceCompleteGame(Player winner) {
		runInLock(() -> {
			List<Player> lostPlayersNow = new ArrayList<>(this.players);
			lostPlayersNow.remove(winner);
			lostPlayersNow.removeAll(this.lostPlayers.getCache());
			lostPlayers.publish(lostPlayersNow);

			List<Player> lastView = new ArrayList<>(this.lostPlayers.getCache());
			lastView.add(winner);

			onComplete.publish(lastView);
		});
	}

	@Override
	public MonoObservable<List<Player>> onComplete() {
		return onComplete.asObservable().toMono();
	}

	@Override
	public Lock getLock() {
		return lock;
	}

	private void initBoard() {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				CellInfo cellInfo = board[i][j];
				Player player = cellInfo.getPlayer();
				if (player != null) {
					changePointsForPlayer(player, cellInfo.getPoints());
				}
				board[i][j] = cellInfo;
			}
		}
	}

	private CellChange turn(Point rootPoint, Player player) {
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

	private void changePointsForPlayer(Player player, int points) {
		if (player != null) {
			playerSummaryPoints.put(player, playerSummaryPoints.get(player) + points);
			summaryStatistics.publish(Collections.unmodifiableMap(playerSummaryPoints));
		}
	}

	private void checkLoses() {
		Iterator<Entry<Player, Integer>> iterator = playerSummaryPoints.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Player, Integer> entry = iterator.next();
			Player player = entry.getKey();
			Integer points = entry.getValue();
			if (points == 0) {
				iterator.remove();
				if (currentPlayer == player) {
					this.currentPlayer = previousPlayer();
				}
				lostPlayers.publish(player);
			}
		}
	}

	private void checkWinner() {
		if (lostPlayers.getCache().size() == this.players.size() - 1) { // 1 player left
			List<Player> leftPlayers = new ArrayList<>(this.players);
			leftPlayers.removeAll(lostPlayers.getCache());
			Player player = leftPlayers.get(0);
			forceCompleteGame(player);
		}
	}

	private Player nextPlayer() {
		int currentPlayerIndex = players.indexOf(this.currentPlayer);
		if (currentPlayerIndex == players.size() - 1) {
			return players.get(0);
		}
		else {
			return players.get(currentPlayerIndex + 1);
		}
	}

	private Player previousPlayer() {
		int currentPlayerIndex = players.indexOf(this.currentPlayer);
		if (currentPlayerIndex == 0) {
			return players.get(players.size() - 1);
		}
		else {
			return players.get(currentPlayerIndex - 1);
		}
	}

	@Override
	public boolean isStarted() {
		return runInLock(() -> isStarted);
	}

	@Override
	public boolean isCompleted() {
		return runInLock(() -> onComplete.getValue() != null);
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

	private static class PointWithCellChange {
		public final Point point;
		public final CellChange cellChange;

		public PointWithCellChange(Point point, CellChange cellChange) {
			this.point = point;
			this.cellChange = cellChange;
		}
	}
}
