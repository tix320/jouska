package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.application.game.GamePlayer;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class GameCompleteDto implements GameChangeDto {

	private final GamePlayer winner;

	@JsonCreator
	public GameCompleteDto(@JsonProperty("winner") GamePlayer winner) {
		this.winner = winner;
	}

	public GamePlayer getWinner() {
		return winner;
	}
}
