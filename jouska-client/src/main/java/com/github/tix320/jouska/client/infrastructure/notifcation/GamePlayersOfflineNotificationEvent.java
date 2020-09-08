package com.github.tix320.jouska.client.infrastructure.notifcation;

import com.github.tix320.jouska.client.infrastructure.UI.NotificationType;
import com.github.tix320.jouska.core.dto.GamePlayersOfflineWarning;
import com.github.tix320.skimp.api.object.None;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public class GamePlayersOfflineNotificationEvent extends NotificationEvent<GamePlayersOfflineWarning, None> {

	public GamePlayersOfflineNotificationEvent(GamePlayersOfflineWarning data) {
		super(NotificationType.GAME_PLAYERS_OFFLINE, data);
	}
}
