package com.github.tix320.jouska.bot;

import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameBoards;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class BotGameEndpoint {

	@Endpoint
	public void notifyGameStarted(GamePlayDto gamePlayDto) {
		GameSettings gameSettings = gamePlayDto.getGameSettings();
		List<InGamePlayer> players = gamePlayDto.getPlayers();
		GameBoard board = GameBoards.createByType(gameSettings.getBoardType(),
				players.stream().map(InGamePlayer::getColor).collect(Collectors.toList()));
		Game game = SimpleGame.createPredefined(board, players);
		PlayerColor myColor = gamePlayDto.getMyPlayer();

		new Bot(gamePlayDto.getGameId(), game, myColor);
	}
}
