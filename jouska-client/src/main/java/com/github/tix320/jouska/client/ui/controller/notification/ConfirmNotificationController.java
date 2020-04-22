package com.github.tix320.jouska.client.ui.controller.notification;

import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class ConfirmNotificationController<T extends NotificationEvent<?, ?>>
		implements NotificationController<T> {

	@FXML
	protected Label notificationTextLabel;

	protected T event;

	@Override
	public abstract void init(T event);

	@Override
	public void destroy() {
	}

	protected final void setConfirmText(String text) {
		notificationTextLabel.setText(text);
	}

	@FXML
	protected abstract void onAccept();

	@FXML
	protected abstract void onReject();
}
