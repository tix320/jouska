package com.github.tix320.jouska.client.infrastructure;

import com.github.tix320.jouska.core.model.Player;

public class CurrentUserContext {

	private static volatile Player player;

	public static Player getPlayer() {
		return player;
	}

	public static void setPlayer(Player player) {
		CurrentUserContext.player = player;
		System.out.printf("I am %s%n", player);
	}
}
