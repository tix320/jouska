package com.github.tix320.jouska.client.infrastructure.notifcation;

import com.github.tix320.jouska.client.ui.controller.notification.TournamentAcceptPlayerNotificationController;
import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public class TournamentJoinNotificationEvent extends NotificationEvent<TournamentJoinRequest, Confirmation> {

	public TournamentJoinNotificationEvent(TournamentJoinRequest data) {
		super(TournamentAcceptPlayerNotificationController.class, data);
	}
}
