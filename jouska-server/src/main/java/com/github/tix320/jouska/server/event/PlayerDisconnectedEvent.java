package com.github.tix320.jouska.server.event;

import com.github.tix320.jouska.core.event.Event;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 22-Mar-20.
 */
public class PlayerDisconnectedEvent implements Event {

	private final Player player;

	public PlayerDisconnectedEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
