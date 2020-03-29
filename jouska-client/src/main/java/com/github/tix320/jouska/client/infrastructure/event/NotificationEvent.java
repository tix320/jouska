package com.github.tix320.jouska.client.infrastructure.event;

import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.core.event.Event;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public class NotificationEvent implements Event {

	private final ComponentType componentType;

	private final Object data;

	public NotificationEvent(ComponentType componentType, Object data) {
		this.componentType = componentType;
		this.data = data;
	}

	public ComponentType getComponentType() {
		return componentType;
	}

	public Object getData() {
		return data;
	}
}
