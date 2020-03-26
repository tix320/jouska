package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.game.Point;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
public class PlayerTurnDto extends GameChangeDto {

	private final Point point;

	@JsonCreator
	public PlayerTurnDto(@JsonProperty("point") Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}
}
