package com.github.tix320.jouska.client.infrastructure.notifcation;

import com.github.tix320.jouska.client.ui.controller.notification.NotificationController;
import com.github.tix320.jouska.core.event.Event;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class NotificationEvent<I, O> implements Event {

	private final Class<? extends NotificationController<?>> controllerClass;

	private final I data;

	private final MonoPublisher<O> resolver;

	public NotificationEvent(Class<? extends NotificationController<?>> controllerClass, I data) {
		this.controllerClass = controllerClass;
		this.data = data;
		this.resolver = Publisher.mono();
	}

	public Class<? extends NotificationController<?>> getControllerClass() {
		return controllerClass;
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
