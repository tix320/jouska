package com.github.tix320.jouska.bot;

import java.util.List;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.GamePlayer;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.SimpleGame;
import com.github.tix320.jouska.core.application.game.creation.SimpleGameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.dto.GameSettingsDto;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class BotGameEndpoint {

	@Endpoint
	public void notifyGameStarted(GamePlayDto gamePlayDto) {
		GameSettingsDto gameSettings = gamePlayDto.getGameSettings();
		List<GamePlayer> players = gamePlayDto.getPlayers();
		Game game = SimpleGame.create(
				(SimpleGameSettings) ((TimedGameSettings) gameSettings.toModel()).getWrappedGameSettings());
		players.forEach(game::addPlayer);
		PlayerColor myColor = gamePlayDto.getMyPlayer();

		new Bot(gamePlayDto.getGameId(), game, myColor);
	}

	@Endpoint
	public Confirmation notifyGameStartingSoon(String gameName) {
		return Confirmation.ACCEPT;
	}
}
