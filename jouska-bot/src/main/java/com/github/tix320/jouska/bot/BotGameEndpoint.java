package com.github.tix320.jouska.bot;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameBoards;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class BotGameEndpoint {

	private volatile long gameId;

	private volatile Game game;

	private volatile PlayerColor myPlayer;

	@Endpoint
	public void notifyGameStarted(GamePlayDto gamePlayDto) {
		this.gameId = gamePlayDto.getGameId();
		GameSettings gameSettings = gamePlayDto.getGameSettings();
		List<InGamePlayer> players = gamePlayDto.getPlayers();
		GameBoard board = GameBoards.createByType(gameSettings.getBoardType(),
				players.stream().map(InGamePlayer::getColor).collect(Collectors.toList()));
		Game game = SimpleGame.createPredefined(board, players);
		PlayerColor myColor = gamePlayDto.getMyPlayer();
		this.game = game;
		this.myPlayer = myColor;

		BotInGameService gameService = BotApp.SONDER_CLIENT.getRPCService(BotInGameService.class);
		gameService.changes(gameId).subscribe(this::onChange);

		game.start();
		tryTurn();
	}

	private void onChange(GameChangeDto gameChange) {
		if (gameChange instanceof PlayerTurnDto) {
			PlayerTurnDto playerTurn = (PlayerTurnDto) gameChange;

			game.turn(playerTurn.getPoint());
			List<InGamePlayer> losers = game.getLostPlayers();
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
			Point turn = BotApp.BOT.turn(game.getBoard(), myPlayer);
			BotApp.SONDER_CLIENT.getRPCService(BotInGameService.class).turn(gameId, turn);
		}
	}
}
