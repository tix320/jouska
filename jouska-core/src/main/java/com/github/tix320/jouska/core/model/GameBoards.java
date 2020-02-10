package com.github.tix320.jouska.core.model;

import java.util.List;

public final class GameBoards {

	public static GameBoard defaultBoard(Player[] players) {
		int height = 8;
		int width = 12;

		int penultRowIndex;
		switch (players.length) {
			case 1:
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 1), new CellInfo(players[0], 3))));
			case 2:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, width / 2 - 1), new CellInfo(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width / 2), new CellInfo(players[1], 3))));
			case 3:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, width / 2 - 1), new CellInfo(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width / 2), new CellInfo(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 1), new CellInfo(players[2], 3))));
			case 4:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 4), new CellInfo(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, 4), new CellInfo(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 7), new CellInfo(players[2], 3)),
								new CellDomination(new Point(1, 7), new CellInfo(players[3], 3))));
			default:
				throw new IllegalArgumentException(
						String.format("Players must be 1,2,3,4, but was %s", players.length));
		}
	}

	public static GameBoard testBoard(Player[] players) {
		int height = 8;
		int width = 12;

		int penultRowIndex;
		switch (players.length) {
			case 1:
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 1), new CellInfo(players[0], 3))));
			case 2:
				return createBoard(height, width,
						List.of(new CellDomination(new Point(height / 2, width / 2), new CellInfo(players[0], 3)),
								new CellDomination(new Point(height / 2 + 1, width / 2), new CellInfo(players[1], 3))));
			case 3:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, width / 2 - 1), new CellInfo(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width / 2), new CellInfo(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 1), new CellInfo(players[2], 3))));
			case 4:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 1), new CellInfo(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width - 2), new CellInfo(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 1), new CellInfo(players[2], 3)),
								new CellDomination(new Point(1, width - 2), new CellInfo(players[3], 3))));
			default:
				throw new IllegalArgumentException(
						String.format("Players must be 1,2,3,4, but was %s", players.length));
		}
	}

	private static GameBoard createBoard(int height, int width, List<CellDomination> cellsDomination) {
		CellInfo[][] cellInfos = new CellInfo[height][width];
		for (int i = 0; i < cellInfos.length; i++) {
			for (int j = 0; j < cellInfos[i].length; j++) {
				cellInfos[i][j] = new CellInfo(null, 0);
			}
		}

		for (CellDomination cellDomination : cellsDomination) {
			Point point = cellDomination.point;
			CellInfo cellInfo = cellDomination.cellInfo;
			cellInfos[point.i][point.j] = cellInfo;
		}
		return new GameBoard(cellInfos);
	}

	private static class CellDomination {
		private final Point point;
		private final CellInfo cellInfo;

		private CellDomination(Point point, CellInfo cellInfo) {
			this.point = point;
			this.cellInfo = cellInfo;
		}
	}
}
