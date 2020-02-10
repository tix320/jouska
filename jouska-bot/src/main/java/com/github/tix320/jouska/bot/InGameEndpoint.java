package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("in-game")
public class InGameEndpoint {

	@Endpoint("turn")
	public void turn(Point point) {
		long gameId = Context.gameId;
		Bot bot = Context.bot;
		JouskaGame game = Context.game;
		Player myPlayer = Context.myPlayer;

		game.turn(point);
		Player currentPlayer = game.getCurrentPlayer();
		if (currentPlayer == myPlayer) {
			Try.run(() -> Thread.sleep(1000));
			Point turn = bot.turn(game.getBoard());
			BotApp.CLONDER.getRPCService(InGameService.class).turn(gameId, turn);
		}
	}

	@Endpoint("lose")
	public void lose(Player player) {
	}

	@Endpoint("win")
	public void win(Player player) {
	}
}

