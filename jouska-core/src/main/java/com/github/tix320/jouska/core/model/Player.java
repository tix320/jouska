package com.github.tix320.jouska.core.model;

import java.util.Objects;

public class Player {

	private final String id;

	private final String nickname;

	private final RoleName role;

	private Player() {
		this(null, null, null);
	}

	public Player(String id, String nickname, RoleName role) {
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

	public RoleName getRole() {
		return role;
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
