package com.github.tix320.jouska.client.infrastructure;


import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;

public class ControllerCommunicator {

	private static final Publisher<StartGameCommand> startGameCommandPublisher = Publisher.single();

	public static Observable<StartGameCommand> startGameCommand() {
		return startGameCommandPublisher.asObservable();
	}

	public static void publishStartGameCommand(StartGameCommand startGameCommand) {
		startGameCommandPublisher.publish(startGameCommand);
	}
}
