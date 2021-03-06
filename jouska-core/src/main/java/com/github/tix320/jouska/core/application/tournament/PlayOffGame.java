package com.github.tix320.jouska.core.application.tournament;

import java.util.Optional;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GamePlayer;
import com.github.tix320.jouska.core.model.Player;

public final class PlayOffGame {
	private final Player firstPlayer;
	private final Player secondPlayer;
	private final Game game;
	private final int realPLayersToBe;

	public PlayOffGame(Player firstPlayer, Player secondPlayer, Game game, int realPLayersToBe) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.game = game;
		this.realPLayersToBe = realPLayersToBe;
		if (realPLayersToBe < 0 || realPLayersToBe > 2) {
			throw new IllegalArgumentException();
		}
	}

	public int getRealPLayersToBe() {
		return realPLayersToBe;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public Game getGame() {
		return game;
	}

	public int getWinnerNumber() {
		Optional<GamePlayer> winner = Optional.ofNullable(game).flatMap(Game::getWinner);
		if (winner.isPresent()) {
			if (winner.get().getRealPlayer().equals(firstPlayer)) {
				return 1;
			}
			else {
				return 2;
			}
		}
		else {
			return -1;
		}
	}
}
