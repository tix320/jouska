package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 24-Mar-20.
 */
public class PlayerLeaveDto extends GameChangeDto {

	private final Player player;

	@JsonCreator
	public PlayerLeaveDto(@JsonProperty("player") Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
