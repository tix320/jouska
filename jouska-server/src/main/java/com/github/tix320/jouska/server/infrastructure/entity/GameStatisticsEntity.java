package com.github.tix320.jouska.server.infrastructure.entity;

import java.util.Map;

import com.github.tix320.jouska.core.application.game.PlayerColor;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class GameStatisticsEntity {

	private Map<PlayerColor, Integer> summaryPoints;

	private GameStatisticsEntity() {
	}

	public GameStatisticsEntity(Map<PlayerColor, Integer> summaryPoints) {
		this.summaryPoints = summaryPoints;
	}

	public Map<PlayerColor, Integer> getSummaryPoints() {
		return summaryPoints;
	}
}
