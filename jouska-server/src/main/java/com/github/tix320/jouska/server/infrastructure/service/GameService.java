package com.github.tix320.jouska.server.infrastructure.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.entity.GameEntity;
import com.github.tix320.jouska.server.entity.GameStatisticsSubEntity;
import com.github.tix320.jouska.server.entity.PlayerEntity;
import org.bson.types.ObjectId;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class GameService {

	public static void saveGame(Game game) {
		if (!game.isCompleted()) {
			throw new IllegalArgumentException("Game not completed");
		}
		List<InGamePlayer> players = game.getPlayers();
		GameStatisticsSubEntity gameStatistics = new GameStatisticsSubEntity(
				adaptStatistics(game.getStatistics().summaryPoints()));

		List<PlayerEntity> playerEntities = players.stream()
				.map(inGamePlayer -> new PlayerEntity(new ObjectId(inGamePlayer.getRealPlayer().getId())))
				.collect(Collectors.toList());

		List<GameChange> changes = game.changes()
				.takeWhile(gameChange -> !(gameChange instanceof GameComplete))
				.toList()
				.get(Duration.ofSeconds(5));
		GameEntity gameEntity = new GameEntity(playerEntities, players, changes, gameStatistics);
		DataSource.INSTANCE.save(gameEntity);
	}

	private static Map<PlayerColor, Integer> adaptStatistics(Map<InGamePlayer, Integer> statistics) {
		return statistics.entrySet().stream().collect(toMap(entry -> entry.getKey().getColor(), Entry::getValue));
	}
}
