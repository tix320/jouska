package com.github.tix320.jouska.client.infrastructure.event;

import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.core.event.Event;

public class MenuContentChangeEvent implements Event {

	private final MenuContentType menuContentType;

	private final Object data;

	public MenuContentChangeEvent(MenuContentType menuContentType) {
		this(menuContentType, null);
	}

	public MenuContentChangeEvent(MenuContentType menuContentType, Object data) {
		this.menuContentType = menuContentType;
		this.data = data;
	}

	public MenuContentType getMenuContentType() {
		return menuContentType;
	}

	public Object getData() {
		return data;
	}
}
