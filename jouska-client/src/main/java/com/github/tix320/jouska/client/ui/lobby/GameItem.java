package com.github.tix320.jouska.client.ui.lobby;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.dto.GameSettingsDto;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.dto.TimedGameSettingsDto;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
	private HBox joinAndWatchHolder;

	@FXML
	private HBox startHolder;

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

	@FXML
	private void copyGameId() {
		String id = gameView.getId();
		ClipboardContent content = new ClipboardContent();
		content.putString(String.valueOf(id));
		Clipboard.getSystemClipboard().setContent(content);
	}

	private void initView(GameView gameView) {
		GameSettingsDto gameSettings = gameView.getGameSettings();
		setGameName(gameView);
		setPlayersCount(gameView);
		setTurnDuration(gameSettings);
		setTurnTotalDuration(gameSettings);
		resolveStartButtonAccessibility(gameView);
		if (gameView.getGameSettings().getPlayersCount() != gameView.getConnectedPlayers().size()
			|| gameView.getGameState() == GameState.RUNNING) {
			startButton.setDisable(true);
		}

		if (gameView.getGameState() == GameState.COMPLETED) {
			startHolder.getChildren().remove(startButton);
			joinAndWatchHolder.getChildren().remove(joinButton);
		}

		if (gameView.getGameState() == GameState.INITIAL) {
			watchButton.setDisable(true);
		}
	}

	private void setGameName(GameView gameView) {
		this.gameNameLabel.setText(gameView.getGameSettings().getName());
	}

	private void setPlayersCount(GameView gameView) {
		GameSettingsDto gameSettings = gameView.getGameSettings();
		if (gameView.getGameState() == GameState.COMPLETED) {
			this.playersCountLabel.setText(String.valueOf(gameView.getGameSettings().getPlayersCount()));
			this.getStyleClass().add("gameItemCompleted");
		}
		else {
			this.playersCountLabel.setText(
					gameView.getConnectedPlayers().size() + "/" + gameSettings.getPlayersCount());
		}
	}

	private void setTurnDuration(GameSettingsDto gameSettings) {
		if (gameSettings instanceof TimedGameSettingsDto) {
			this.turnDurationLabel.setText(((TimedGameSettingsDto) gameSettings).getTurnDurationSeconds() + "s");
		}
		else {
			this.turnDurationLabel.setText("-");
		}
	}

	private void setTurnTotalDuration(GameSettingsDto gameSettings) {
		if (gameSettings instanceof TimedGameSettingsDto) {
			this.turnTotalDurationLabel.setText(
					((TimedGameSettingsDto) gameSettings).getPlayerTurnTotalDurationSeconds() / 60 + "m");
		}
		else {
			this.turnTotalDurationLabel.setText("-");
		}
	}

	private void resolveStartButtonAccessibility(GameView gameView) {
		Player creator = gameView.getCreator();
		Player currentPlayer = CurrentUserContext.getPlayer();

		if (!currentPlayer.equals(creator) && !currentPlayer.isAdmin()) {
			startHolder.getChildren().remove(startButton);
		}
	}

	public GameView getGameView() {
		return gameView;
	}

	public void disableJoinButtonOn(ObservableBooleanValue disableProperty) {
		joinButton.disableProperty()
				.bind(new SimpleBooleanProperty(isFull()).or(
						new SimpleBooleanProperty(gameView.getGameState() != GameState.INITIAL)).or(disableProperty));
	}

	private boolean isFull() {
		return gameView.getGameSettings().getPlayersCount() == gameView.getConnectedPlayers().size();
	}
}
