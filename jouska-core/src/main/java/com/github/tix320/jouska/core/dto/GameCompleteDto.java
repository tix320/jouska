package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.game.InGamePlayer;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class GameCompleteDto extends GameChangeDto {

	private final InGamePlayer winner;

	@JsonCreator
	public GameCompleteDto(@JsonProperty("winner") InGamePlayer winner) {
		this.winner = winner;
	}

	public InGamePlayer getWinner() {
		return winner;
	}
}
