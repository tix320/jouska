package com.github.tix320.jouska.core.game;

import java.util.Map;

import com.github.tix320.jouska.core.model.InGamePlayer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface Statistics {

	/**
	 * Returns observable which will listen of player's point changes on board.
	 */
	Observable<Map<InGamePlayer, Integer>> summaryPoints();
}
