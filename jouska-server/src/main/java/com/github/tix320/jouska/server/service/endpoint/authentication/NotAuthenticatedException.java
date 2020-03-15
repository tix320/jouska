package com.github.tix320.jouska.server.service.endpoint.authentication;

public class NotAuthenticatedException extends RuntimeException {

	public NotAuthenticatedException(String message) {
		super(message);
	}
}
