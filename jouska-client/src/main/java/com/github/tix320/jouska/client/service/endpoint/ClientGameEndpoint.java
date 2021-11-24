package com.github.tix320.jouska.client.service.endpoint;

import java.time.Duration;

import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.client.infrastructure.notifcation.GamePlayersOfflineNotificationEvent;
import com.github.tix320.jouska.client.infrastructure.notifcation.GameStartingSoonEvent;
import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.dto.GamePlayersOfflineWarning;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.observable.TimeoutException;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("game")
public class ClientGameEndpoint {

	@Endpoint
	public void notifyGamePlayersOffline(GamePlayersOfflineWarning warning) {
		GamePlayersOfflineNotificationEvent event = new GamePlayersOfflineNotificationEvent(warning);
		EventDispatcher.fire(event, NotificationEvent.class);
	}

	@Endpoint
	public Confirmation notifyGameStartingSoon(String gameName) {
		GameStartingSoonEvent event = new GameStartingSoonEvent(gameName);
		EventDispatcher.fire(event, NotificationEvent.class);

		try {
			return event.onResolve().get(Duration.ofSeconds(30));
		} catch (TimeoutException e) {
			return Confirmation.REJECT;
		}
	}

	@Endpoint
	public void notifyGameStarted(GamePlayDto gamePlayDto) {
		EventDispatcher.fire(new GameStartedEvent(gamePlayDto));
	}
}
