package com.github.tix320.jouska.client.ui.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.ControllerCommunicator;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.client.ui.lobby.GameItem;
import com.github.tix320.jouska.core.dto.GameView;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.GAME_SERVICE;

public class LobbyController implements Controller<Object> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private FlowPane gameItemsPane;

	@FXML
	private Label waitingPlayersLabel;

	private Timeline timeline;

	@Override
	public void initialize(Object data) {
		gameItemsPane.disableProperty().bind(loading);
		refresh();
		loading.addListener((observable, oldValue, newValue) -> MenuController.SELF.loadingProperty().set(newValue));
	}

	@FXML
	void refresh() {
		fetchGames();
	}

	private void fetchGames() {
		loading.set(true);
		GAME_SERVICE.getGames().subscribe(gameViews -> {
			List<GameItem> gameItems = gameViews.stream().map(GameItem::new).collect(Collectors.toList());
			Collections.reverse(gameItems);
			gameItems.forEach(gameItem -> gameItem.setOnMouseClicked(event -> onItemClick(gameItem, event)));
			Platform.runLater(() -> {
				ObservableList<Node> gameList = gameItemsPane.getChildren();
				gameList.clear();
				gameList.addAll(gameItems);
			});
			loading.set(false);
		});
	}

	private void onItemClick(GameItem gameItem, MouseEvent event) {
		if (event.getClickCount() == 2) {
			GameView gameView = gameItem.getGameView();
			long gameId = gameView.getId();
			loading.set(true);
			timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), ae -> {
				String text = waitingPlayersLabel.getText();
				int indexOfDot = text.indexOf('.');
				int dotsCount = text.substring(indexOfDot).length();
				String realText = text.substring(0, indexOfDot);
				if (dotsCount == 3) {
					waitingPlayersLabel.setText(realText + '.');
				}
				else {
					waitingPlayersLabel.setText(realText + ".".repeat(dotsCount + 1));
				}
			}));

			timeline.setCycleCount(Animation.INDEFINITE);
			timeline.play();
			waitingPlayersLabel.setVisible(true);
			GAME_SERVICE.connect(gameId).subscribe(answer -> {
				switch (answer) {
					case GAME_NOT_FOUND:
						waitingPlayersLabel.setVisible(false);
					case ALREADY_STARTED:
						Platform.runLater(() -> {
							Alert alert = new Alert(AlertType.CONFIRMATION);
							alert.setTitle("Confirmation");
							alert.setHeaderText("Game is started.");
							alert.setContentText("Game already started. Do you want to watch it?");

							Optional<ButtonType> result = alert.showAndWait();
							if (result.isPresent() && result.get() == ButtonType.OK) {
								GAME_SERVICE.watch(gameId);
							}
							else {
								loading.set(false);
							}
						});
						break;
					case CONNECTED:
						ControllerCommunicator.startGameCommand().toMono().subscribe(startGameCommand -> {
							waitingPlayersLabel.setVisible(false);
							timeline.stop();
							JouskaUI.switchScene(ComponentType.GAME, startGameCommand);
						});
						break;
					default:
				}
			});
		}
	}
}
