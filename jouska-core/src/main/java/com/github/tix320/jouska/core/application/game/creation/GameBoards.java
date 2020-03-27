package com.github.tix320.jouska.core.application.game.creation;

import java.util.List;

import com.github.tix320.jouska.core.application.game.*;

/**
 * Factory of boards.
 */
public final class GameBoards {

	public static GameBoard createByType(BoardType boardType, List<PlayerColor> playerColors) {
		switch (boardType) {
			case STANDARD:
				return defaultBoard(playerColors.toArray(PlayerColor[]::new));
			case TEST:
				return testBoard(playerColors.toArray(PlayerColor[]::new));
			default:
				throw new IllegalArgumentException();
		}
	}

	public static GameBoard defaultBoard(PlayerColor[] players) {
		int height = 8;
		int width = 12;

		int penultRowIndex;
		switch (players.length) {
			case 1:
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 1), new BoardCell(players[0], 3))));
			case 2:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, width / 2 - 1), new BoardCell(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width / 2), new BoardCell(players[1], 3))));
			case 3:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, width / 2 - 1), new BoardCell(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width / 2), new BoardCell(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 1), new BoardCell(players[2], 3))));
			case 4:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 4), new BoardCell(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, 4), new BoardCell(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 7), new BoardCell(players[2], 3)),
								new CellDomination(new Point(1, 7), new BoardCell(players[3], 3))));
			default:
				throw new IllegalArgumentException(
						String.format("Players must be 1,2,3,4, but was %s", players.length));
		}
	}

	public static GameBoard testBoard(PlayerColor[] players) {
		int height = 8;
		int width = 12;

		int penultRowIndex;
		switch (players.length) {
			case 1:
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 1), new BoardCell(players[0], 3))));
			case 2:
				return createBoard(height, width,
						List.of(new CellDomination(new Point(height / 2, width / 2), new BoardCell(players[0], 3)),
								new CellDomination(new Point(height / 2 + 1, width / 2), new BoardCell(players[1], 3))));
			case 3:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, width / 2 - 1), new BoardCell(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width / 2), new BoardCell(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 1), new BoardCell(players[2], 3))));
			case 4:
				penultRowIndex = height - 2;
				return createBoard(height, width,
						List.of(new CellDomination(new Point(1, 1), new BoardCell(players[0], 3)),
								new CellDomination(new Point(penultRowIndex, width - 2), new BoardCell(players[1], 3)),
								new CellDomination(new Point(penultRowIndex, 1), new BoardCell(players[2], 3)),
								new CellDomination(new Point(1, width - 2), new BoardCell(players[3], 3))));
			default:
				throw new IllegalArgumentException(
						String.format("Players must be 1,2,3,4, but was %s", players.length));
		}
	}

	private static GameBoard createBoard(int height, int width, List<CellDomination> cellsDomination) {
		BoardCell[][] boardCells = new BoardCell[height][width];
		for (int i = 0; i < boardCells.length; i++) {
			for (int j = 0; j < boardCells[i].length; j++) {
				boardCells[i][j] = new BoardCell(null, 0);
			}
		}

		for (CellDomination cellDomination : cellsDomination) {
			Point point = cellDomination.point;
			BoardCell boardCell = cellDomination.boardCell;
			boardCells[point.i][point.j] = boardCell;
		}
		return new GameBoard(boardCells);
	}

	private static class CellDomination {
		private final Point point;
		private final BoardCell boardCell;

		private CellDomination(Point point, BoardCell boardCell) {
			this.point = point;
			this.boardCell = boardCell;
		}
	}
}
