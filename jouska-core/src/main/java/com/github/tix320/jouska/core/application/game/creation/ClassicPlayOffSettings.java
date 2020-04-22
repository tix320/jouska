package com.github.tix320.jouska.core.application.game.creation;

import java.util.List;

import com.github.tix320.jouska.core.application.tournament.ClassicPlayOff;
import com.github.tix320.jouska.core.application.tournament.RestorablePlayOff;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 25-Apr-20.
 */
public class ClassicPlayOffSettings implements RestorablePlayOffSettings {

	private final GameSettings baseGameSettings;

	private ClassicPlayOffSettings() {
		this(null);
	}

	public ClassicPlayOffSettings(GameSettings baseGameSettings) {
		this.baseGameSettings = baseGameSettings;
	}

	@Override
	public GameSettings getBaseGameSettings() {
		return baseGameSettings;
	}

	@Override
	public RestorablePlayOffSettings changeBaseGameSettings(GameSettings gameSettings) {
		return new ClassicPlayOffSettings(gameSettings);
	}

	@Override
	public RestorablePlayOff createPlayOff(List<Player> players) {
		return ClassicPlayOff.create(players, this);
	}
}
