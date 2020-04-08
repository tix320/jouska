package com.github.tix320.jouska.bot;

import java.util.List;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.dto.GameChangeDto;
import com.github.tix320.jouska.core.dto.GameCompleteDto;
import com.github.tix320.jouska.core.dto.PlayerLeaveDto;
import com.github.tix320.jouska.core.dto.PlayerTurnDto;
import com.github.tix320.kiwi.api.check.Try;

/**
 * @author Tigran Sargsyan on 07-Apr-20.
 */
public final class Bot {

	private final long gameId;

	private final Game game;

	private final PlayerColor myPlayer;

	// private final BotGameOrigin gameService;

	public Bot(long gameId, Game game, PlayerColor myPlayer) {
		this.gameId = gameId;
		this.game = game;
		this.myPlayer = myPlayer;
		game.start();
		tryTurn();
		// gameService.changes(gameId).subscribe(this::onChange);
		// this.gameService = gameService;
	}

	private void onChange(GameChangeDto gameChange) {
		if (gameChange instanceof PlayerTurnDto) {
			PlayerTurnDto playerTurn = (PlayerTurnDto) gameChange;

			game.turn(playerTurn.getPoint());
			List<InGamePlayer> losers = game.getLosers();
			for (InGamePlayer loser : losers) {
				if (loser.getColor().equals(myPlayer)) {
					// TODO Bot lose
					break;
				}
			}

			tryTurn();
		}
		else if (gameChange instanceof PlayerLeaveDto) {
			PlayerLeaveDto playerLeave = (PlayerLeaveDto) gameChange;
			game.kick(playerLeave.getPlayer());
		}
		else if (gameChange instanceof GameCompleteDto) {
			GameCompleteDto gameComplete = (GameCompleteDto) gameChange;
			if (!game.isCompleted()) {
				game.forceCompleteGame(gameComplete.getWinner().getRealPlayer());
			}
		}
		else {
			throw new IllegalArgumentException();
		}
	}

	private void tryTurn() {
		if (!game.isCompleted() && game.getCurrentPlayer().getColor() == myPlayer) {
			Try.runOrRethrow(() -> Thread.sleep(1000));
			// Point turn = BotApp.BOT.turn(game.getBoard(), myPlayer);
			// gameService.turn(gameId, turn);
		}
	}
}
