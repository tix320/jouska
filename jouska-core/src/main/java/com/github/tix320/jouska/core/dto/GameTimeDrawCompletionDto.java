package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class GameTimeDrawCompletionDto extends TimedGameChangeDto {

	private final int additionalSeconds;

	@JsonCreator
	public GameTimeDrawCompletionDto(@JsonProperty("additionalSeconds") int additionalSeconds) {
		this.additionalSeconds = additionalSeconds;
	}

	public int getAdditionalSeconds() {
		return additionalSeconds;
	}
}
