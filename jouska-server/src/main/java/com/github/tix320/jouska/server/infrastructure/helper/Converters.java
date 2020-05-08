package com.github.tix320.jouska.server.infrastructure.helper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.GamePlayer;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.entity.GamePlayerEntity;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class Converters {

	public static GamePlayer gamePlayerEntityToInGamePlayer(GamePlayerEntity gamePlayerEntity) {
		return new GamePlayer(playerEntityToPLayer(gamePlayerEntity.getPlayerEntity()),
				gamePlayerEntity.getPlayerColor());
	}

	public static List<GamePlayer> gamePlayerEntityToInGamePlayer(List<GamePlayerEntity> gamePlayerEntities) {
		return gamePlayerEntities.stream().map(Converters::gamePlayerEntityToInGamePlayer).collect(Collectors.toList());
	}

	public static Player playerEntityToPLayer(PlayerEntity playerEntity) {
		if (playerEntity == null) {
			return null;
		}
		return new Player(playerEntity.getId(), playerEntity.getNickname(), playerEntity.getRole());
	}

	public static Set<Player> playerEntityToPLayer(Collection<PlayerEntity> playerEntities) {
		return playerEntities.stream().map(Converters::playerEntityToPLayer).collect(Collectors.toSet());
	}
}
