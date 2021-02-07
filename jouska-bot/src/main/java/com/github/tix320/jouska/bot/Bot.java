package com.github.tix320.jouska.bot;

import java.util.List;

import com.github.tix320.jouska.bot.process.BotProcess;
import com.github.tix320.jouska.bot.service.origin.BotGameOrigin;
import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.dto.GameChangeDto;
import com.github.tix320.jouska.core.dto.GameCompleteDto;
import com.github.tix320.jouska.core.dto.PlayerLeaveDto;
import com.github.tix320.jouska.core.dto.PlayerTimedTurnDto;

/**
 * @author Tigran Sargsyan on 07-Apr-20.
 */
public final class Bot {

	private final String gameId;

	private final Game game;

	private final PlayerColor myPlayer;

	private final BotGameOrigin gameService;

	private final BotProcess botProcess;

	public Bot(String gameId, Game game, PlayerColor myPlayer, BotGameOrigin gameService, BotProcess botProcess) {
		this.gameId = gameId;
		this.game = game;
		this.myPlayer = myPlayer;
		this.gameService = gameService;
		this.botProcess = botProcess;
		game.start();
		ReadOnlyGameBoard board = game.getBoard();
		botProcess.startGame(board.getHeight(), board.getWidth());
		gameService.changes(gameId).conditionalSubscribe(this::onChange);
		game.completed().subscribe(g -> botProcess.endGame());
		tryTurn();
	}

	private boolean onChange(GameChangeDto gameChange) {
		if (gameChange instanceof PlayerTimedTurnDto) {
			PlayerTimedTurnDto playerTurn = (PlayerTimedTurnDto) gameChange;

			game.turn(playerTurn.getPoint());
			List<GamePlayer> losers = game.getLosers();
			for (GamePlayer loser : losers) {
				if (loser.getColor().equals(myPlayer)) {
					botProcess.endGame();
					return false;
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

		return true;
	}

	private void tryTurn() {
		if (!game.isCompleted() && game.getCurrentPlayer().getColor() == myPlayer) {
			Point turn = botProcess.turn(game.getBoard(), myPlayer);
			gameService.turn(gameId, turn);
		}
	}
}
