package com.github.tix320.jouska.core.dto;

import com.github.tix320.jouska.core.model.Player;

/**
 * @author tigra on 04-Apr-20.
 */
public class PlayOffGameView {

	private final Player firstPlayer;

	private final Player secondPlayer;

	private final int winner; // -1 - not completed, 1 - winner is first, 2 - winner is second

	private PlayOffGameView() {
		this(null, null, -1);
	}

	public PlayOffGameView(Player firstPlayer, Player secondPlayer, int winner) {
		if (winner != -1 && winner != 1 && winner != 2) {
			throw new IllegalArgumentException();
		}
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.winner = winner;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public int getWinner() {
		return winner;
	}
}
