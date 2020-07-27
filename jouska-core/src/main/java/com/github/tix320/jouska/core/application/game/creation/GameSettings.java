package com.github.tix320.jouska.core.application.game.creation;

import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.Game;

/**
 * NOTE: Implementation must be Immutable.
 */
public interface GameSettings {

	String getName();

	BoardType getBoardType();

	int getPlayersCount();

	GameSettings changeName(String name);

	GameSettings changePlayersCount(int playersCount);

	/**
	 * Create game by this settings
	 */
	Game createGame();
}
