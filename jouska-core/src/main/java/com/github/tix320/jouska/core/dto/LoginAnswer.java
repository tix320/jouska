package com.github.tix320.jouska.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class LoginAnswer {

	private final LoginResult loginResult;

	private final Player player; // null if LoginResult not success

	@JsonCreator
	public LoginAnswer(@JsonProperty("loginResult") LoginResult loginResult, @JsonProperty("player") Player player) {
		this.loginResult = loginResult;
		this.player = player;
	}

	public LoginResult getLoginResult() {
		return loginResult;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return "LoginAnswer{" + "loginResult=" + loginResult + ", player=" + player + '}';
	}
}
