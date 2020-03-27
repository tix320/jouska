package com.github.tix320.jouska.server.infrastructure.endpoint.auth;

public class NotAuthenticatedException extends RuntimeException {

	public NotAuthenticatedException(String message) {
		super(message);
	}
}
