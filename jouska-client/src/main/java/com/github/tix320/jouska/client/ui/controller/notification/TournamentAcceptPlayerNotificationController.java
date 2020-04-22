package com.github.tix320.jouska.client.ui.controller.notification;

import com.github.tix320.jouska.client.infrastructure.notifcation.TournamentJoinNotificationEvent;
import com.github.tix320.jouska.core.dto.Confirmation;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class TournamentAcceptPlayerNotificationController
		extends ConfirmNotificationController<TournamentJoinNotificationEvent> {

	@Override
	public void init(TournamentJoinNotificationEvent event) {
		this.event = event;
		String nickname = event.getData().getPlayer().getNickname();
		String tournamentName = event.getData().getTournamentView().getName();
		setConfirmText(String.format("`%s` want to join to your tournament `%s`", nickname, tournamentName));
	}

	public void onAccept() {
		event.resolve(Confirmation.ACCEPT);
	}

	public void onReject() {
		event.resolve(Confirmation.REJECT);
	}
}
