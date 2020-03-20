package com.github.tix320.jouska.client.infrastructure.event.game;

import com.github.tix320.jouska.client.infrastructure.event.Event;
import com.github.tix320.jouska.core.model.Point;

public class TurnEvent implements Event {

	private final Point point;

	public TurnEvent(Point point) {
		this.point = point;
	}

	public Point getPoint() {
		return point;
	}
}
