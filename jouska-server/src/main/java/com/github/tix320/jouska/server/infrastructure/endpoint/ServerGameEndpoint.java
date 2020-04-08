package com.github.tix320.jouska.server.infrastructure.endpoint;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.jouska.core.dto.GameChangeDto;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Endpoint("in-game")
public class ServerGameEndpoint {

	@Endpoint
	@Subscription
	public Observable<GameChangeDto> changes(long gameId, @CallerUser Player player) {
		Game game = GameManager.getGame(gameId, player);
		return game.changes().map(GameChangeDto::fromModel);
	}

	@Endpoint
	public void turn(long gameId, Point point, @CallerUser Player player) {
		GameManager.turnInGame(gameId, player, point);
	}
}
