package com.github.tix320.jouska.core.application.tournament;

import java.util.List;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.creation.RestorableGroupSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public interface RestorableGroup extends Group {

	@Override
	RestorableGroupSettings getSettings();

	/**
	 * Restore group state from given structure.
	 *
	 * @throws RestoreException if cannot restore now for some reasons.
	 */
	void restore(List<Game> games) throws RestoreException;
}
