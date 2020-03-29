package com.github.tix320.jouska.client.infrastructure.event;

import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.event.Event;

public class GameStartedEvent implements Event {

	private final GamePlayDto gamePlayDto;

	public GameStartedEvent(GamePlayDto gamePlayDto) {
		this.gamePlayDto = gamePlayDto;
	}

	public GamePlayDto getGamePlayDto() {
		return gamePlayDto;
	}
}
