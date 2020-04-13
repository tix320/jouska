package com.github.tix320.jouska.core.application.game;

import java.util.List;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class GameComplete extends GameChange {

	private final InGamePlayer winner;

	private final List<InGamePlayer> losers;

	private GameComplete() {
		this(null, null);
	}

	public GameComplete(InGamePlayer winner, List<InGamePlayer> losers) {
		this.winner = winner;
		this.losers = losers;
	}

	public InGamePlayer getWinner() {
		return winner;
	}

	public List<InGamePlayer> getLosers() {
		return losers;
	}
}
