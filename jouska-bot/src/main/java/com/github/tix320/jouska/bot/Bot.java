package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.game.BoardCell;
import com.github.tix320.jouska.core.game.PlayerColor;
import com.github.tix320.jouska.core.game.Point;

public final class Bot {

	private final PlayerColor player;

	public Bot(PlayerColor player) {
		this.player = player;
	}

	public Point turn(BoardCell[][] board) {
		for (int i = 0; i < board.length; i++) {
			BoardCell[] boardCells = board[i];
			for (int j = 0; j < boardCells.length; j++) {
				BoardCell boardCell = boardCells[j];
				if (boardCell.getColor() == player) {
					return new Point(i, j);
				}
			}
		}
		throw new IllegalStateException();
	}
}
