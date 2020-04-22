package com.github.tix320.jouska.server.infrastructure.entity;

import dev.morphia.annotations.Reference;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class PlayOffGameEntity {

	@Reference
	private PlayerEntity firstPlayer;

	@Reference
	private PlayerEntity secondPlayer;

	@Reference
	private GameEntity game;

	private PlayOffGameEntity() {
	}

	public PlayOffGameEntity(PlayerEntity firstPlayer, PlayerEntity secondPlayer, GameEntity game) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.game = game;
	}

	public PlayerEntity getFirstPlayer() {
		return firstPlayer;
	}

	public PlayerEntity getSecondPlayer() {
		return secondPlayer;
	}

	public GameEntity getGame() {
		return game;
	}
}
