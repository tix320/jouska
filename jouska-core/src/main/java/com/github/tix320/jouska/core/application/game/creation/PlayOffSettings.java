package com.github.tix320.jouska.core.application.game.creation;

import java.util.List;

import com.github.tix320.jouska.core.application.tournament.PlayOff;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 25-Apr-20.
 */
public interface PlayOffSettings {

	GameSettings getBaseGameSettings();

	PlayOffSettings changeBaseGameSettings(GameSettings gameSettings);

	PlayOff createPlayOff(List<Player> players);
}
