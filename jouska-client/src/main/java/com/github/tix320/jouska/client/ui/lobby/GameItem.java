package com.github.tix320.jouska.client.ui.lobby;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class GameItem extends AnchorPane {

	@FXML
	private Label gameNameLabel;

	@FXML
	private Label playersCountLabel;

	@FXML
	private Label turnDurationLabel;

	@FXML
	private Label turnTotalDurationLabel;

	@FXML
	private Button joinButton;

	@FXML
	private Button startButton;

	@FXML
	private HBox startButtonHolder;

	private final GameView gameView;

	public GameItem(GameView gameView) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/lobby/game-item.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		this.gameView = gameView;
		initView(gameView);
	}

	public void setOnJoinClick(EventHandler<? super MouseEvent> handler) {
		joinButton.setOnMouseClicked(handler);
	}

	public void setOnStartClick(EventHandler<? super MouseEvent> handler) {
		startButton.setOnMouseClicked(handler);
	}

	private void initView(GameView gameView) {
		setGameName(gameView.getName() + " ( " + gameView.getId() + " )");
		setPlayersCount(gameView.getPlayersCount() + "/" + gameView.getMaxPlayersCount());
		setTurnDuration(String.valueOf(gameView.getTurnDurationSeconds()));
		setTurnTotalDuration(String.valueOf(gameView.getPlayerTurTotalDurationSeconds() / 60));
		resolveStartButtonAccessibility(gameView);
		if (gameView.getPlayersCount() != gameView.getMaxPlayersCount()) {
			startButton.setDisable(true);
		}
	}

	private void setGameName(String gameName) {
		this.gameNameLabel.setText(gameName);
	}

	private void setPlayersCount(String playerCount) {
		this.playersCountLabel.setText(playerCount);
	}

	private void setTurnDuration(String turnDuration) {
		this.turnDurationLabel.setText(turnDuration + "s");
	}

	private void setTurnTotalDuration(String gameDuration) {
		this.turnTotalDurationLabel.setText(gameDuration + "m");
	}

	private void resolveStartButtonAccessibility(GameView gameView) {
		Player creator = gameView.getCreator();
		Player currentPlayer = CurrentUserContext.getPlayer();

		if (!currentPlayer.equals(creator) && !currentPlayer.isAdmin()) {
			startButtonHolder.setDisable(true);
			startButtonHolder.setVisible(false);
		}
	}

	public Button getJoinButton() {
		return joinButton;
	}

	public GameView getGameView() {
		return gameView;
	}
}
