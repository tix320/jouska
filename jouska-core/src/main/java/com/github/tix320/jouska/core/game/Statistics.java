package com.github.tix320.jouska.core.game;

import java.util.Map;

import com.github.tix320.jouska.core.model.InGamePlayer;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public interface Statistics {

	/**
	 * Returns observable which will listen tof player's point changes on board.
	 */
	ReadOnlyProperty<Map<InGamePlayer, Integer>> summaryPoints();
}
