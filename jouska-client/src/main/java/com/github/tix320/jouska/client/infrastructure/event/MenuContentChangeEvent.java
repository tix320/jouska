package com.github.tix320.jouska.client.infrastructure.event;

import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.core.event.Event;

public class MenuContentChangeEvent implements Event {

	private final MenuContentType menuContentType;

	public MenuContentChangeEvent(MenuContentType menuContentType) {
		this.menuContentType = menuContentType;
	}

	public MenuContentType getMenuContentType() {
		return menuContentType;
	}
}
