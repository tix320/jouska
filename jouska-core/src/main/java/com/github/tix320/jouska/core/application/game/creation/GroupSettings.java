package com.github.tix320.jouska.core.application.game.creation;

import java.util.Set;

import com.github.tix320.jouska.core.application.tournament.Group;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 25-Apr-20.
 */
public interface GroupSettings {

	GameSettings getBaseGameSettings();

	GroupSettings changeBaseGameSettings(GameSettings gameSettings);

	Group createGroup(Set<Player> players);
}
