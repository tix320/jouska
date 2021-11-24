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
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import com.github.tix320.sonder.api.common.rpc.Subscription;

@Endpoint("in-game")
public class ServerGameEndpoint {

	private final GameDao gameDao;

	private final GameManager gameManager;

	public ServerGameEndpoint(GameDao gameDao, GameManager gameManager) {
		this.gameDao = gameDao;
		this.gameManager = gameManager;
	}

	@Endpoint
	@Subscription
	public Observable<GameChangeDto> changes(String gameId, @CallerUser Player player) {
		Optional<Game> game = gameManager.getGame(gameId, player);
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
		gameManager.turnInGame(gameId, player, point);
	}
}
