package com.github.tix320.jouska.core.application.game;

import java.util.Objects;

import com.github.tix320.jouska.core.model.Player;

public final class InGamePlayer {

	private final Player realPlayer;

	private final PlayerColor color;

	private InGamePlayer() {
		this(null, null);
	}

	public InGamePlayer(Player realPlayer, PlayerColor color) {
		this.realPlayer = realPlayer;
		this.color = color;
	}

	public Player getRealPlayer() {
		return realPlayer;
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
		return realPlayer.equals(that.realPlayer) && color == that.color;
	}

	@Override
	public int hashCode() {
		return Objects.hash(realPlayer, color);
	}

	@Override
	public String toString() {
		return realPlayer.getNickname() + " : " + color;
	}
}
