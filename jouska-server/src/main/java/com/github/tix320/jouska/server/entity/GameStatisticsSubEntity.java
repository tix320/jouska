package com.github.tix320.jouska.server.entity;

import java.util.Map;

import com.github.tix320.jouska.core.application.game.PlayerColor;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class GameStatisticsSubEntity {

	private Map<PlayerColor, Integer> summaryPoints;

	public GameStatisticsSubEntity() {
	}

	public GameStatisticsSubEntity(Map<PlayerColor, Integer> summaryPoints) {
		this.summaryPoints = summaryPoints;
	}

	public Map<PlayerColor, Integer> getSummaryPoints() {
		return summaryPoints;
	}
}
