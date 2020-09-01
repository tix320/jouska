package com.github.tix320.jouska.core.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public final class Player {

	private final String id;

	private final String nickname;

	private final Role role;

	private Player() {
		this(null, null, null);
	}

	public Player(String id, String nickname, Role role) {
		this.id = id;
		this.nickname = nickname;
		this.role = role;
	}

	public String getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public Role getRole() {
		return role;
	}

	@JsonIgnore
	public boolean isAdmin() {
		return role == Role.ADMIN;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Player player = (Player) o;
		return id.equals(player.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return nickname;
	}
}
