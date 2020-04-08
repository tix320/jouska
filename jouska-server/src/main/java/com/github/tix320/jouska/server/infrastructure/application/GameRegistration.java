package com.github.tix320.jouska.server.infrastructure.application;

import com.github.tix320.jouska.core.application.game.GameWithSettings;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author tigra on 05-Apr-20.
 */
public class GameRegistration {

	private final GameWithSettings gameWithSettings;

	private final Player creator;

	public GameRegistration(GameWithSettings gameWithSettings, Player creator) {
		this.gameWithSettings = gameWithSettings;
		this.creator = creator;
	}

	public GameWithSettings getGameWithSettings() {
		return gameWithSettings;
	}

	public Player getCreator() {
		return creator;
	}
}
