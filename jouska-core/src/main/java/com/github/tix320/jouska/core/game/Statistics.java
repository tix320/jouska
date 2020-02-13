package com.github.tix320.jouska.core.game;

import java.util.Map;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface Statistics {

	/**
	 * Returns observable which will listen tof player's point changes on board.
	 */
	Observable<Map<Player, Integer>> summaryPoints();
}
