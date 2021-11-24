package com.github.tix320.jouska.core.event;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.publisher.Publisher;

public class EventDispatcher {

	private static final Map<Class<? extends Event>, Publisher<Event>> publishers = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends Event> Observable<T> on(Class<T> clazz) {
		Publisher<Event> publisher = publishers.computeIfAbsent(clazz, eventClass -> Publisher.simple());

		return (Observable<T>) publisher.asObservable();
	}

	public static void fire(Event eventObject) {
		Publisher<Event> publisher = publishers.computeIfAbsent(eventObject.getClass(),
				eventClass -> Publisher.simple());
		publisher.publish(eventObject);
	}

	public static void fire(Event eventObject, Class<? extends Event> as) {
		if (!as.isAssignableFrom(eventObject.getClass())) {
			throw new IllegalArgumentException(
					String.format("%s is not assignable from %s", as, eventObject.getClass()));
		}
		Publisher<Event> publisher = publishers.computeIfAbsent(as, eventClass -> Publisher.simple());
		publisher.publish(eventObject);
	}
}
