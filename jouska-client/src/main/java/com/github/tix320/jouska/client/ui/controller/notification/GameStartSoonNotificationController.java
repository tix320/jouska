package com.github.tix320.jouska.client.ui.controller.notification;

import com.github.tix320.jouska.client.infrastructure.notifcation.GameStartingSoonEvent;
import com.github.tix320.jouska.core.dto.Confirmation;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public class GameStartSoonNotificationController extends ConfirmNotificationController<GameStartingSoonEvent> {

	@Override
	public void init(GameStartingSoonEvent event) {
		this.event = event;
		String gameName = event.getData();
		setConfirmText(String.format("Game `%s` starting soon, are you ready?", gameName));
	}

	@Override
	protected void onAccept() {
		event.resolve(Confirmation.ACCEPT);
	}

	@Override
	protected void onReject() {
		event.resolve(Confirmation.REJECT);
	}
}
