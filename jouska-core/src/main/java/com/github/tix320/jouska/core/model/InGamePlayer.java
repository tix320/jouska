package com.github.tix320.jouska.core.model;

import java.util.Objects;

public final class InGamePlayer {

	private final Player player;

	private final PlayerColor color;

	private InGamePlayer() {
		this(null, null);
	}

	public InGamePlayer(Player player, PlayerColor color) {
		this.player = player;
		this.color = color;
	}

	public Player getPlayer() {
		return player;
	}

	public PlayerColor getColor() {
		return color;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InGamePlayer that = (InGamePlayer) o;
		return color == that.color;
	}

	@Override
	public int hashCode() {
		return Objects.hash(color);
	}

	@Override
	public String toString() {
		return player.getNickname() + " : " + color;
	}
}
