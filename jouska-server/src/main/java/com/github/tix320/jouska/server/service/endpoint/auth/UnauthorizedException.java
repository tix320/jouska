package com.github.tix320.jouska.server.service.endpoint.auth;

public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException(String message) {
		super(message);
	}
}
