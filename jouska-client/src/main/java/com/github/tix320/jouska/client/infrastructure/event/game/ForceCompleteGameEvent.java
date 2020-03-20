package com.github.tix320.jouska.client.infrastructure.event.game;

import com.github.tix320.jouska.client.infrastructure.event.Event;
import com.github.tix320.jouska.core.model.Player;

public class ForceCompleteGameEvent implements Event {

	private final Player winner;

	public ForceCompleteGameEvent(Player winner) {
		this.winner = winner;
	}

	public Player getWinner() {
		return winner;
	}
}
