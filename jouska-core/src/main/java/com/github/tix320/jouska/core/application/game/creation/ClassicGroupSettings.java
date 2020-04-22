package com.github.tix320.jouska.core.application.game.creation;

import java.util.Set;

import com.github.tix320.jouska.core.application.tournament.ClassicGroup;
import com.github.tix320.jouska.core.application.tournament.RestorableGroup;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 25-Apr-20.
 */
public class ClassicGroupSettings implements RestorableGroupSettings {

	private final GameSettings baseGameSettings;

	private ClassicGroupSettings() {
		this(null);
	}

	public ClassicGroupSettings(GameSettings baseGameSettings) {
		this.baseGameSettings = baseGameSettings;
	}

	@Override
	public GameSettings getBaseGameSettings() {
		return baseGameSettings;
	}

	@Override
	public RestorableGroupSettings changeBaseGameSettings(GameSettings gameSettings) {
		return new ClassicGroupSettings(gameSettings);
	}

	@Override
	public RestorableGroup createGroup(Set<Player> players) {
		return ClassicGroup.create(players, this);
	}
}
