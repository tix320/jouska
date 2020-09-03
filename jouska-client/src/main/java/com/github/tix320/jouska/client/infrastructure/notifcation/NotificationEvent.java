package com.github.tix320.jouska.client.infrastructure.notifcation;

import com.github.tix320.jouska.client.infrastructure.UI.NotificationType;
import com.github.tix320.jouska.core.event.Event;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class NotificationEvent<I, O> implements Event {

	private final NotificationType notificationType;

	private final I data;

	private final MonoPublisher<O> resolver;

	public NotificationEvent(NotificationType notificationType, I data) {
		this.notificationType = notificationType;
		this.data = data;
		this.resolver = Publisher.mono();
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public I getData() {
		return data;
	}

	public void resolve(O data) {
		resolver.publish(data);
	}

	public MonoObservable<O> onResolve() {
		return resolver.asObservable();
	}
}
