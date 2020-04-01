package com.github.tix320.jouska.client.ui.lobby;

import java.util.Set;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class GameItem extends AnchorPane {

	@FXML
	private AnchorPane gameItem;

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

	private void initView(GameView gameView) {
		setGameName(gameView.getName() + " ( " + gameView.getId() + " )");
		setPlayersCount(gameView.getPlayersCount() + "/" + gameView.getMaxPlayersCount());
		setTurnDuration(String.valueOf(gameView.getTurnDurationSeconds()));
		setTurnTotalDuration(String.valueOf(gameView.getPlayerTurTotalDurationSeconds() / 60));
		setPublicPrivateBackground(gameView.getAccessedPlayers());
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

	private void setPublicPrivateBackground(Set<Player> accessedPlayers) {
		Background publicBackground = new Background(
				new BackgroundFill(Color.web("#82be3c"), CornerRadii.EMPTY, Insets.EMPTY));

		Background privateBackground = new Background(
				new BackgroundFill(Color.web("#be5b45"), CornerRadii.EMPTY, Insets.EMPTY));
		if (accessedPlayers.isEmpty()) {
			gameItem.setBackground(publicBackground);
		}
		else {
			Player currentPlayer = CurrentUserContext.getPlayer();
			if (gameView.getCreator().equals(currentPlayer)) {
				gameItem.setBackground(publicBackground);
			}
			else if (accessedPlayers.contains(currentPlayer)) {
				gameItem.setBackground(publicBackground);
			}
			else {
				gameItem.setBackground(privateBackground);
			}
		}
	}


	public GameView getGameView() {
		return gameView;
	}
}
