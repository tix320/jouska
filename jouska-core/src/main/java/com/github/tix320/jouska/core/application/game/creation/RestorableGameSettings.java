package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.RestorableGame;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public interface RestorableGameSettings extends GameSettings {

	@Override
	RestorableGameSettings changeName(String name);

	@Override
	RestorableGameSettings changePlayersCount(int playersCount);

	@Override
	RestorableGame createGame();
}
