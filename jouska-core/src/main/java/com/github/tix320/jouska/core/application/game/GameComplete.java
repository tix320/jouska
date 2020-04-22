package com.github.tix320.jouska.core.application.game;

import java.util.List;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class GameComplete implements GameChange {

	private final GamePlayer winner;

	private final List<GamePlayer> losers;

	private GameComplete() {
		this(null, null);
	}

	public GameComplete(GamePlayer winner, List<GamePlayer> losers) {
		this.winner = winner;
		this.losers = losers;
	}

	public GamePlayer getWinner() {
		return winner;
	}

	public List<GamePlayer> getLosers() {
		return losers;
	}

	@Override
	public void accept(GameChangeVisitor visitor) {
		visitor.visit(this);
	}
}
