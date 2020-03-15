package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.model.PlayerColor;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("in-game")
public class BotInGameEndpoint {

	@Endpoint("canTurn")
	public void canTurn()
			throws InterruptedException {
		long gameId = Context.gameId;
		Bot bot = Context.bot;
		Game game = Context.game;
		PlayerColor myPlayer = Context.myPlayer;
		if (game.getCurrentPlayer().getColor() == myPlayer) {
			Thread.sleep(1000);
			Point turn = bot.turn(game.getBoard());
			BotApp.SONDER_CLIENT.getRPCService(BotInGameService.class).turn(gameId, turn);
		}
	}

	@Endpoint("turn")
	public void turn(Point point) {
		Game game = Context.game;

		game.turn(point);
	}

	@Endpoint("lose")
	public void lose(PlayerColor player) {
	}

	@Endpoint("win")
	public void win(PlayerColor player) {
	}
}

