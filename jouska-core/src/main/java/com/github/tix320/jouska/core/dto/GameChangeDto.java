package com.github.tix320.jouska.core.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.tix320.jouska.core.application.game.GameChange;
import com.github.tix320.jouska.core.application.game.GameComplete;
import com.github.tix320.jouska.core.application.game.PlayerKick;
import com.github.tix320.jouska.core.application.game.PlayerTimedTurn;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
@JsonSubTypes({
		@JsonSubTypes.Type(value = GameCompleteDto.class, name = "complete"),
		@JsonSubTypes.Type(value = PlayerLeaveDto.class, name = "leave"),
		@JsonSubTypes.Type(value = PlayerTimedTurnDto.class, name = "turn")})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface GameChangeDto {

	static GameChangeDto fromModel(GameChange gameChange) {
		if (gameChange instanceof PlayerTimedTurn) {
			PlayerTimedTurn playerTurn = (PlayerTimedTurn) gameChange;
			return new PlayerTimedTurnDto(playerTurn.getPoint(), playerTurn.getRemainingTurnMillis(),
					playerTurn.getRemainingPlayerTotalTurnMillis());
		}
		else if (gameChange instanceof PlayerKick) {
			PlayerKick playerKick = (PlayerKick) gameChange;
			return new PlayerLeaveDto(playerKick.getPlayerWithPoints().getPlayer().getRealPlayer());
		}
		else if (gameChange instanceof GameComplete) {
			GameComplete gameComplete = (GameComplete) gameChange;
			return new GameCompleteDto(gameComplete.getWinner());
		}
		else {
			throw new IllegalArgumentException(gameChange.getClass().getSimpleName());
		}
	}
}
