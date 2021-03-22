package com.github.tix320.jouska.server.infrastructure.entity;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Reference;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
@Entity
public class PlayOffGameEntity {

	@Reference
	private PlayerEntity firstPlayer;

	@Reference
	private PlayerEntity secondPlayer;

	@Reference
	private GameEntity game;

	private int realPLayersToBe;

	private PlayOffGameEntity() {
	}

	public PlayOffGameEntity(PlayerEntity firstPlayer, PlayerEntity secondPlayer, GameEntity game,
							 int realPLayersToBe) {
		this.firstPlayer = firstPlayer;
		this.secondPlayer = secondPlayer;
		this.game = game;
		this.realPLayersToBe = realPLayersToBe;
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

	public int getRealPLayersToBe() {
		return realPLayersToBe;
	}
}
