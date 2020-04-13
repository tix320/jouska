package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.application.game.Point;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayerTimedTurnDto extends PlayerTurnDto implements TimedGameChangeDto {

	private final long remainingTurnMillis;

	private final long remainingPlayerTotalTurnMillis;

	@JsonCreator
	public PlayerTimedTurnDto(@JsonProperty("point") Point point,
							  @JsonProperty("remainingTurnMillis") long remainingTurnMillis,
							  @JsonProperty("remainingPlayerTotalTurnMillis") long remainingPlayerTotalTurnMillis) {
		super(point);
		this.remainingTurnMillis = remainingTurnMillis;
		this.remainingPlayerTotalTurnMillis = remainingPlayerTotalTurnMillis;
	}

	public long getRemainingTurnMillis() {
		return remainingTurnMillis;
	}

	public long getRemainingPlayerTotalTurnMillis() {
		return remainingPlayerTotalTurnMillis;
	}
}
