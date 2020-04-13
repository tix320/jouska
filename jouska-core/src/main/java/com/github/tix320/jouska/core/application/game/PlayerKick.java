package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
public class PlayerKick implements GameChange {

	private final PlayerWithPoints playerWithPoints;

	private PlayerKick() {
		this(null);
	}

	public PlayerKick(PlayerWithPoints playerWithPoints) {
		this.playerWithPoints = playerWithPoints;
	}

	public PlayerWithPoints getPlayerWithPoints() {
		return playerWithPoints;
	}
}
