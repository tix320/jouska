package com.github.tix320.jouska.core.infrastructure;

/**
 * @author Tigran Sargsyan on 23-Apr-20.
 */
public class RestoreException extends RuntimeException {

	public RestoreException(String message) {
		super(message);
	}

	public RestoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
