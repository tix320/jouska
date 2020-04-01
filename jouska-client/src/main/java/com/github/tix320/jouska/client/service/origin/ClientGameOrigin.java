package com.github.tix320.jouska.client.service.origin;

import com.github.tix320.jouska.core.dto.GameChangeDto;
import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("in-game")
public interface ClientGameOrigin {

	@Origin("changes")
	@Subscription
	Observable<GameChangeDto> changes(long gameId);

	@Origin("makeTurn")
	void turn(long gameId, Point point);
}
