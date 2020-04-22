package com.github.tix320.jouska.client.infrastructure.notifcation;

import com.github.tix320.jouska.client.infrastructure.UI.NotificationType;
import com.github.tix320.jouska.core.dto.Confirmation;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public class GameStartingSoonEvent extends NotificationEvent<String, Confirmation> {

	public GameStartingSoonEvent(String data) {
		super(NotificationType.GAME_START_SOON, data);
	}
}
