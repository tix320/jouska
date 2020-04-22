package com.github.tix320.jouska.core.application.game;

import java.util.List;

import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;
import com.github.tix320.jouska.core.infrastructure.UnsupportedChangeException;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public interface RestorableGame extends Game {

	@Override
	RestorableGameSettings getSettings();

	/**
	 * Restore game state from changes.
	 *
	 * @throws RestoreException if not in state {@link GameState#INITIAL}
	 */
	void restore(List<GameChange> changes) throws UnsupportedChangeException, RestoreException;
}
