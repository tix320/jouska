package com.github.tix320.jouska.core.application.game;

import java.util.Map;

public interface Statistics {

	/**
	 * Returns player's point on board.
	 */
	Map<InGamePlayer, Integer> summaryPoints();
}
