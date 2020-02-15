package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;

public final class Bot {

	private final Player player;

	public Bot(Player player) {
		this.player = player;
	}

	public Point turn(CellInfo[][] board) {
		for (int i = 0; i < board.length; i++) {
			CellInfo[] cellInfos = board[i];
			for (int j = 0; j < cellInfos.length; j++) {
				CellInfo cellInfo = cellInfos[j];
				if (cellInfo.getPlayer() == player) {
					return new Point(i, j);
				}
			}
		}
		throw new IllegalStateException();
	}
}