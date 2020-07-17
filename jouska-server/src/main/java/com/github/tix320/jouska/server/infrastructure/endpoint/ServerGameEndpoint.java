package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.util.Optional;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.jouska.core.dto.GameChangeDto;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.application.GameManager;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.entity.GameEntity;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Endpoint("in-game")
public class ServerGameEndpoint {

	private final GameDao gameDao = new GameDao();

	@Endpoint
	@Subscription
	public Observable<GameChangeDto> changes(String gameId, @CallerUser Player player) {
		Optional<Game> game = GameManager.getGame(gameId, player);
		if (game.isPresent()) {
			return game.get().changes().asObservable().map(GameChangeDto::fromModel);
		}
		else {
			GameEntity gameEntity = gameDao.findById(gameId)
					.orElseThrow(() -> new IllegalArgumentException(String.format("Game %s not found", gameId)));
			return Observable.of(gameEntity.getChanges()).map(GameChangeDto::fromModel);
		}
	}

	@Endpoint
	public void turn(String gameId, Point point, @CallerUser Player player) {
		GameManager.turnInGame(gameId, player, point);
	}
}
