package com.github.tix320.jouska.core.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.tix320.jouska.core.application.game.*;

/**
 * @author Tigran Sargsyan on 25-Mar-20.
 */
@JsonSubTypes({
		@JsonSubTypes.Type(value = GameCompleteDto.class, name = "complete"),
		@JsonSubTypes.Type(value = PlayerLeaveDto.class, name = "leave"),
		@JsonSubTypes.Type(value = PlayerTurnDto.class, name = "turn"),
		@JsonSubTypes.Type(value = GameTimeDrawCompletionDto.class, name = "drawComplete")})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public abstract class GameChangeDto {

	public static GameChangeDto fromModel(GameChange gameChange) {
		if (gameChange instanceof PlayerTurn) {
			PlayerTurn playerTurn = (PlayerTurn) gameChange;
			return new PlayerTurnDto(playerTurn.getCellChange().getPoint());
		}
		else if (gameChange instanceof PlayerKick) {
			PlayerKick playerKick = (PlayerKick) gameChange;
			return new PlayerLeaveDto(playerKick.getPlayerWithPoints().getPlayer().getRealPlayer());
		}
		else if (gameChange instanceof GameComplete) {
			GameComplete gameComplete = (GameComplete) gameChange;
			return new GameCompleteDto(gameComplete.getWinner());
		}
		else if (gameChange instanceof GameTimeDrawCompletion) {
			GameTimeDrawCompletion drawComplete = (GameTimeDrawCompletion) gameChange;
			return new GameTimeDrawCompletionDto(drawComplete.getAdditionalSeconds());
		}
		else {
			throw new IllegalArgumentException();
		}
	}
}
