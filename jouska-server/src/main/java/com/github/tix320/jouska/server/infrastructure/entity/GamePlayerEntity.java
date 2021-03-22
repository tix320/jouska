package com.github.tix320.jouska.server.infrastructure.entity;

import com.github.tix320.jouska.core.application.game.PlayerColor;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
@Entity
public class GamePlayerEntity {

	@Reference
	private PlayerEntity playerEntity;

	private PlayerColor playerColor;

	private GamePlayerEntity() {
	}

	public GamePlayerEntity(PlayerEntity playerEntity, PlayerColor playerColor) {
		this.playerEntity = playerEntity;
		this.playerColor = playerColor;
	}

	public PlayerEntity getPlayerEntity() {
		return playerEntity;
	}

	public PlayerColor getPlayerColor() {
		return playerColor;
	}
}
