package com.github.tix320.jouska.core.application.tournament;

import java.util.List;

import com.github.tix320.jouska.core.application.game.creation.RestorablePlayOffSettings;
import com.github.tix320.jouska.core.infrastructure.RestoreException;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public interface RestorablePlayOff extends PlayOff {

	@Override
	RestorablePlayOffSettings getSettings();

	/**
	 * Restore play-off state from given structure.
	 *
	 * @throws RestoreException if cannot restore now for some reasons.
	 */
	void restore(List<List<PlayOffGame>> structure) throws RestoreException;
}
