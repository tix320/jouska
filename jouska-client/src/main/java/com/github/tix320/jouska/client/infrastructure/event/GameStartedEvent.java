package com.github.tix320.jouska.client.infrastructure.event;

import com.github.tix320.jouska.core.dto.StartGameCommand;

public class GameStartedEvent implements Event {

	private final StartGameCommand startGameCommand;

	public GameStartedEvent(StartGameCommand startGameCommand) {
		this.startGameCommand = startGameCommand;
	}

	public StartGameCommand getStartGameCommand() {
		return startGameCommand;
	}
}
