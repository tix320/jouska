package com.github.tix320.jouska.client.service;

import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.sonder.api.common.rpc.Origin;

@Origin("in-game")
public interface InGameService {

	@Origin("turn")
	void turn(long gameId, Point point);
}
