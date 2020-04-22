package com.github.tix320.jouska.client.ui.controller.notification;

import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public abstract class WarningNotificationController<T extends NotificationEvent<?, ?>>
		implements NotificationController<T> {

	@FXML
	private Label notificationTextLabel;

	protected T event;

	@Override
	public abstract void init(T event);

	@Override
	public void destroy() {
	}

	protected final void setWarningText(String text) {
		notificationTextLabel.setText(text);
	}

	@FXML
	protected abstract void onAccept();
}
