package com.github.tix320.jouska.core.application.tournament;

import java.util.List;

import com.github.tix320.jouska.core.infrastructure.RestoreException;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public interface RestorableTournament extends Tournament {

	@Override
	List<RestorableGroup> getGroups();

	@Override
	ReadOnlyProperty<RestorablePlayOff> playOff();

	/**
	 * Restore tournament state from given groups and play-off.
	 *
	 * @throws RestoreException if cannot restore now for some reasons.
	 */
	void restore(List<Group> groups, PlayOff playOff) throws RestoreException;
}
