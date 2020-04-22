package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.application.game.GamePlayer;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;

public final class GamePlayDto extends GameWatchDto {

	private final PlayerColor myPlayer;

	private GamePlayDto() {
		myPlayer = null;
	}

	public GamePlayDto(String gameId, GameSettingsDto gameSettings, List<GamePlayer> players, PlayerColor myPlayer) {
		super(gameId, gameSettings, players);
		this.myPlayer = myPlayer;
	}

	public PlayerColor getMyPlayer() {
		return myPlayer;
	}
}
