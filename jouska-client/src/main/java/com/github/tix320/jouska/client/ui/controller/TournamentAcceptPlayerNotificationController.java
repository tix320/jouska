package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.core.dto.TournamentJoinAnswer;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class TournamentAcceptPlayerNotificationController implements NotificationController<TournamentJoinRequest> {

	@FXML
	private Label notificationTextLabel;

	private MonoPublisher<TournamentJoinAnswer> resolver = Publisher.mono();

	@Override
	public void init(TournamentJoinRequest data) {
		String nickname = data.getPlayer().getNickname();
		String tournamentName = data.getTournamentView().getName();
		notificationTextLabel.setText(
				String.format("`%s` want to join to your tournament `%s`", nickname, tournamentName));
	}

	@Override
	public void destroy() {
		resolver.complete();
	}

	@Override
	public MonoObservable<?> resolved() {
		return resolver.asObservable();
	}

	public void onAccept(ActionEvent event) {
		resolver.publish(TournamentJoinAnswer.ACCEPT);
	}

	public void onReject(ActionEvent event) {
		resolver.publish(TournamentJoinAnswer.REJECT);
	}
}
