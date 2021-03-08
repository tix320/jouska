package com.github.tix320.jouska.client.service.endpoint;

import java.time.Duration;

import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import com.github.tix320.jouska.client.infrastructure.notifcation.TournamentJoinNotificationEvent;
import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

/**
 * @author Tigran Sargsyan on 30-Mar-20.
 */
@Endpoint("tournament")
public class ClientTournamentEndpoint {

	@Endpoint
	public Confirmation requestTournamentJoin(TournamentJoinRequest request) {
		NotificationEvent<TournamentJoinRequest, Confirmation> event = new TournamentJoinNotificationEvent(request);
		EventDispatcher.fire(event, NotificationEvent.class);

		try {
			return event.onResolve().get(Duration.ofSeconds(30));
		} catch (TimeoutException e) {
			System.out.println("Tournament Join notification skipped");
			return Confirmation.REJECT;
		}
	}
}
