package com.github.tix320.jouska.client.ui.lobby;

import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.kiwi.api.check.Try;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class GameItem extends AnchorPane {

	@FXML
	private Label gameNameLabel;

	@FXML
	private Label playersCountLabel;

	@FXML
	private Label turnDurationLabel;

	@FXML
	private Label gameDurationLabel;

	private final GameView gameView;

	public GameItem(GameView gameView) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/lobby/game-item.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		this.gameView = gameView;
		initView(gameView);
	}

	private void initView(GameView gameView) {
		setGameName(gameView.getName());
		setPlayersCount(gameView.getPlayersCount() + "/" + gameView.getMaxPlayersCount());
		setTurnDuration(String.valueOf(gameView.getTurnDurationSeconds()));
		setGameDuration(String.valueOf(gameView.getGameDurationMinutes()));
	}

	private void setGameName(String gameName) {
		this.gameNameLabel.setText(gameName);
	}

	private void setPlayersCount(String playerCount) {
		this.playersCountLabel.setText(playerCount);
	}

	private void setTurnDuration(String turnDuration) {
		this.turnDurationLabel.setText(turnDuration);
	}

	private void setGameDuration(String gameDuration) {
		this.gameDurationLabel.setText(gameDuration);
	}

	public GameView getGameView() {
		return gameView;
	}
}
