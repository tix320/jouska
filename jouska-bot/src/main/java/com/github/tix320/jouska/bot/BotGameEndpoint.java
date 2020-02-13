package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.SimpleJouskaGame;
import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class BotGameEndpoint {

	@Endpoint("start")
	public void startGame(StartGameCommand startGameCommand) {
		long gameId = startGameCommand.getGameId();
		JouskaGame jouskaGame = SimpleJouskaGame.create(startGameCommand.getGameBoard(),
				startGameCommand.getPlayers());
		Player myPlayer = startGameCommand.getMyPlayer();
		Bot bot = new Bot(myPlayer);
		Context.gameId = gameId;
		Context.bot = bot;
		Context.game = jouskaGame;
		Context.myPlayer = myPlayer;
		if (jouskaGame.getCurrentPlayer() == myPlayer) {
			Point turn = bot.turn(jouskaGame.getBoard());
			jouskaGame.turn(turn);
			BotApp.CLONDER.getRPCService(BotInGameService.class).turn(gameId, turn);
		}
	}
}
