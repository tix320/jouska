package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.SimpleGame;
import com.github.tix320.jouska.core.model.PlayerColor;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class BotGameEndpoint {

	@Endpoint("start")
	public void startGame(StartGameCommand startGameCommand) {
		long gameId = startGameCommand.getGameId();
		Game game = SimpleGame.create(startGameCommand.getGameSettings(), startGameCommand.getGameBoard(),
				startGameCommand.getPlayers());
		PlayerColor myPlayer = startGameCommand.getMyPlayer();
		Bot bot = new Bot(myPlayer);
		Context.gameId = gameId;
		Context.bot = bot;
		Context.game = game;
		Context.myPlayer = myPlayer;
		game.start();
		if (game.getCurrentPlayer().getColor() == myPlayer) {
			Point turn = bot.turn(game.getBoard());
			BotApp.SONDER_CLIENT.getRPCService(BotInGameService.class).turn(gameId, turn);
		}
	}
}
