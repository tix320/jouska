package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class TournamentJoinRequest {

	private final TournamentView tournamentView;

	private final Player player;

	@JsonCreator
	public TournamentJoinRequest(@JsonProperty("tournament") TournamentView tournamentView,
								 @JsonProperty("player") Player player) {
		this.tournamentView = tournamentView;
		this.player = player;
	}

	public TournamentView getTournamentView() {
		return tournamentView;
	}

	public Player getPlayer() {
		return player;
	}
}
