package com.github.tix320.jouska.core.application.tournament;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public class TournamentAlreadyFullException extends RuntimeException {

	public TournamentAlreadyFullException(String message) {
		super(message);
	}
}
