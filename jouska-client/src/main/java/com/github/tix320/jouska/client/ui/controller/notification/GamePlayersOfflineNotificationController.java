package com.github.tix320.jouska.client.ui.controller.notification;

import java.util.List;

import com.github.tix320.jouska.client.infrastructure.notifcation.GamePlayersOfflineNotificationEvent;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.skimp.api.object.None;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public class GamePlayersOfflineNotificationController
		extends WarningNotificationController<GamePlayersOfflineNotificationEvent> {

	@Override
	public void init(GamePlayersOfflineNotificationEvent event) {
		this.event = event;
		String gameName = event.getData().getGameName();
		List<Player> offlinePlayers = event.getData().getPlayers();
		setWarningText(String.format("The players %s are offline now for game `%s`", offlinePlayers, gameName));
	}

	@Override
	protected void onAccept() {
		event.resolve(None.SELF);
	}
}
