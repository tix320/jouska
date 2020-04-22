package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
public class GameAlreadyFullException extends RuntimeException {

	public GameAlreadyFullException(String message) {
		super(message);
	}
}
