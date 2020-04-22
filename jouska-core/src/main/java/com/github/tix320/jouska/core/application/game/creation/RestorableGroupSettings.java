package com.github.tix320.jouska.core.application.game.creation;

import java.util.Set;

import com.github.tix320.jouska.core.application.tournament.RestorableGroup;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 25-Apr-20.
 */
public interface RestorableGroupSettings extends GroupSettings {

	@Override
	RestorableGroupSettings changeBaseGameSettings(GameSettings gameSettings);

	@Override
	RestorableGroup createGroup(Set<Player> players);
}
