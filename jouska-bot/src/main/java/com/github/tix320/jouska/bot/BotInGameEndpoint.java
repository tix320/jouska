package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("in-game")
public class BotInGameEndpoint {

	@Endpoint("canTurn")
	public void canTurn() {
		long gameId = Context.gameId;
		Bot bot = Context.bot;
		JouskaGame game = Context.game;
		Player myPlayer = Context.myPlayer;
		if (game.getCurrentPlayer() == myPlayer) {
			Point turn = bot.turn(game.getBoard());
			BotApp.CLONDER.getRPCService(BotInGameService.class).turn(gameId, turn);
		}
	}

	@Endpoint("turn")
	public void turn(Point point) {
		JouskaGame game = Context.game;

		game.turn(point);
	}

	@Endpoint("lose")
	public void lose(Player player) {
	}

	@Endpoint("win")
	public void win(Player player) {
	}
}

