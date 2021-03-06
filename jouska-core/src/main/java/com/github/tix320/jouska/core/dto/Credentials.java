package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Credentials {

	private final String nickname;

	private final String password;

	@JsonCreator
	public Credentials(@JsonProperty("nickname") String nickname, @JsonProperty("password") String password) {
		this.nickname = nickname;
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}
}
