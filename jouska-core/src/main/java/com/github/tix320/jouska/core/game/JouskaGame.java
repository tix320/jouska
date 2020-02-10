package com.github.tix320.jouska.core.game;

import java.util.*;
import java.util.Map.Entry;

import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;

public class JouskaGame {

	private static final int MAX_POINTS = 4;

	private final CellInfo[][] board;

	private final List<Player> players;

	private final List<Point> turnList;

	private final Map<Player, Integer> playerSummaryPoints;

	private final List<Player> losePlayers;

	private Player winPlayer;

	private Player currentPlayer;

	public JouskaGame(GameBoard board, Player[] players) {
		turnList = new ArrayList<>();
		losePlayers = new ArrayList<>(players.length);
		playerSummaryPoints = new EnumMap<>(Player.class);
		for (Player player : players) {
			playerSummaryPoints.put(player, 0);
		}

		CellInfo[][] matrix = board.getMatrix();
		this.board = new CellInfo[matrix.length][matrix[0].length];
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				CellInfo cellInfo = matrix[i][j];
				Player player = cellInfo.getPlayer();
				if (player != null) {
					changePointsForPlayer(player, cellInfo.getPoints());
				}
				this.board[i][j] = cellInfo;
			}
		}
		this.players = new ArrayList<>(Arrays.asList(players));
		this.currentPlayer = players[0];
	}

	public CellChange turn(Point point) {
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

		if (winPlayer != null) {
			throw new IllegalStateException("Game already completed");
		}

		CellChange cellChange = turn(point, player);
		turnList.add(point);
		checkLoses();
		this.currentPlayer = nextPlayer();
		return cellChange;
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

	public CellInfo[][] getBoard() {
		return board;
	}

	public List<Point> getTurnList() {
		return turnList;
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
				losePlayers.add(player);
				if (currentPlayer == player) {
					this.currentPlayer = previousPlayer();
				}
				players.remove(player);
			}
		}
		if (players.size() == 1) {
			this.winPlayer = players.get(0);
		}
	}

	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	public Player playerOf(Point point) {
		return board[point.i][point.j].getPlayer();
	}

	public Map<Player, Integer> getPlayerSummaryPoints() {
		return playerSummaryPoints;
	}

	public List<Player> getLosePlayers() {
		List<Player> losePlayers = new ArrayList<>(this.losePlayers);
		this.losePlayers.clear();
		return losePlayers;
	}

	public Optional<Player> getWinPlayer() {
		return Optional.ofNullable(winPlayer);
	}

	public static class PointWithCellChange {
		public final Point point;
		public final CellChange cellChange;

		public PointWithCellChange(Point point, CellChange cellChange) {
			this.point = point;
			this.cellChange = cellChange;
		}
	}

	public static class CellChange {
		public final Point point;
		public final CellInfo cellInfo;
		public final boolean collapse;
		public final List<CellChange> children;

		public CellChange(Point point, CellInfo cellInfo, boolean collapse, List<CellChange> children) {
			this.point = point;
			this.cellInfo = cellInfo;
			this.collapse = collapse;
			this.children = children;
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
}
