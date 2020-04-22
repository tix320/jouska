package com.github.tix320.jouska.core.application.game.creation;

import java.util.List;

import com.github.tix320.jouska.core.application.tournament.RestorablePlayOff;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 25-Apr-20.
 */
public interface RestorablePlayOffSettings extends PlayOffSettings {

	@Override
	RestorablePlayOffSettings changeBaseGameSettings(GameSettings gameSettings);

	@Override
	RestorablePlayOff createPlayOff(List<Player> players);
}
