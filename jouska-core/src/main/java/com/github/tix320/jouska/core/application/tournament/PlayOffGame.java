package com.github.tix320.jouska.core.application.tournament;

import java.util.Optional;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GameWithSettings;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.model.Player;

public final class PlayOffGame {
	private final Player firstPlayer;
	private final Player secondPlayer;
	private final GameWithSettings gameWithSettings;

	public PlayOffGame(Player firstPlayer, Player secondPlayer, GameWithSettings gameWithSettings) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.gameWithSettings = gameWithSettings;
	}

	public Player getFirstPlayer() {
		return firstPlayer;
	}

	public Player getSecondPlayer() {
		return secondPlayer;
	}

	public GameWithSettings getGameWithSettings() {
		return gameWithSettings;
	}

	public int getWinnerNumber() {
		if (gameWithSettings == null) {
			return -1;
		}

		Game game = gameWithSettings.getGame();
		Optional<InGamePlayer> winner = game.getWinner();
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
