package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class GameTimeDrawCompletion extends TimedGameChange {

	private final int additionalSeconds;

	public GameTimeDrawCompletion(int additionalSeconds) {
		this.additionalSeconds = additionalSeconds;
	}

	public int getAdditionalSeconds() {
		return additionalSeconds;
	}
}
