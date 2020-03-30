package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import com.github.tix320.jouska.core.dto.TournamentJoinAnswer;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class TournamentAcceptPlayerNotificationController
		implements NotificationController<TournamentJoinRequest, TournamentJoinAnswer> {

	@FXML
	private Label notificationTextLabel;

	private NotificationEvent<TournamentJoinRequest, TournamentJoinAnswer> event;

	@Override
	public void init(NotificationEvent<TournamentJoinRequest, TournamentJoinAnswer> event) {
		this.event = event;
		String nickname = event.getData().getPlayer().getNickname();
		String tournamentName = event.getData().getTournamentView().getName();
		notificationTextLabel.setText(
				String.format("`%s` want to join to your tournament `%s`", nickname, tournamentName));
	}

	@Override
	public void destroy() {
	}

	public void onAccept() {
		event.resolve(TournamentJoinAnswer.ACCEPT);
	}

	public void onReject() {
		event.resolve(TournamentJoinAnswer.REJECT);
	}
}
