package com.github.tix320.jouska.bot.service.origin;

import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.jouska.core.dto.GameChangeDto;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Origin("in-game")
public interface BotGameOrigin {

	@Origin
	@Subscription
	Observable<GameChangeDto> changes(String gameId);

	@Origin
	void turn(String gameId, Point point);
}
