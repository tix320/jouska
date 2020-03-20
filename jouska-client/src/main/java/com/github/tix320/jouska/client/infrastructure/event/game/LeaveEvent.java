package com.github.tix320.jouska.client.infrastructure.event.game;

import com.github.tix320.jouska.client.infrastructure.event.Event;
import com.github.tix320.jouska.core.model.Player;

public class LeaveEvent implements Event {

	private final Player player;

	public LeaveEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}
}
