package com.github.tix320.jouska.client.ui.lobby;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

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
	private Button watchButton;

	@FXML
	private Button startButton;

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

	public void setOnWatchClick(EventHandler<? super MouseEvent> handler) {
		watchButton.setOnMouseClicked(handler);
	}

	public void setOnStartClick(EventHandler<? super MouseEvent> handler) {
		startButton.setOnMouseClicked(handler);
	}

	private void initView(GameView gameView) {
		GameSettings gameSettings = gameView.getGameSettings();
		setGameName(gameView);
		setPlayersCount(gameView);
		setTurnDuration(gameSettings);
		setTurnTotalDuration(gameSettings);
		resolveStartButtonAccessibility(gameView);
		if (gameView.getGameSettings().getPlayersCount() != gameView.getConnectedPlayers().size()
			|| gameView.isStarted()) {
			startButton.setDisable(true);
		}
		if (!gameView.isStarted()) {
			watchButton.setDisable(true);
		}
	}

	private void setGameName(GameView gameView) {
		this.gameNameLabel.setText(gameView.getGameSettings().getName() + " ( " + gameView.getId() + " )");
	}

	private void setPlayersCount(GameView gameView) {
		GameSettings gameSettings = gameView.getGameSettings();
		this.playersCountLabel.setText(gameView.getConnectedPlayers().size() + "/" + gameSettings.getPlayersCount());
	}

	private void setTurnDuration(GameSettings gameSettings) {
		if (gameSettings instanceof TimedGameSettings) {
			this.turnDurationLabel.setText(((TimedGameSettings) gameSettings).getTurnDurationSeconds() + "s");
		}
		else {
			this.turnDurationLabel.setText("-");
		}
	}

	private void setTurnTotalDuration(GameSettings gameSettings) {
		if (gameSettings instanceof TimedGameSettings) {
			this.turnTotalDurationLabel.setText(
					((TimedGameSettings) gameSettings).getPlayerTurnTotalDurationSeconds() / 60 + "m");
		}
		else {
			this.turnTotalDurationLabel.setText("-");
		}
	}

	private void resolveStartButtonAccessibility(GameView gameView) {
		Player creator = gameView.getCreator();
		Player currentPlayer = CurrentUserContext.getPlayer();

		if (!currentPlayer.equals(creator) && !currentPlayer.isAdmin()) {
			startButton.setDisable(true);
			startButton.setVisible(false);
		}
	}

	public GameView getGameView() {
		return gameView;
	}

	public void disableJoinButtonOn(ObservableBooleanValue disableProperty) {
		joinButton.disableProperty()
				.bind(new SimpleBooleanProperty(isFull()).or(new SimpleBooleanProperty(gameView.isStarted()))
						.or(disableProperty));
	}

	private boolean isFull() {
		return gameView.getGameSettings().getPlayersCount() == gameView.getConnectedPlayers().size();
	}
}
