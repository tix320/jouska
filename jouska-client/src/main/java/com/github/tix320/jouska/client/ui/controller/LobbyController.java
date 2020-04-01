package com.github.tix320.jouska.client.ui.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.client.ui.lobby.ConnectedPlayerItem;
import com.github.tix320.jouska.client.ui.lobby.GameItem;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.AUTHENTICATION_SERVICE;
import static com.github.tix320.jouska.client.app.Services.GAME_SERVICE;

public class LobbyController implements Controller<Object> {

	private final SimpleBooleanProperty disable = new SimpleBooleanProperty(false);

	@FXML
	private FlowPane gameItemsPane;

	@FXML
	private Label waitingPlayersLabel;

	@FXML
	private Button cancelWaitButton;

	@FXML
	private VBox connectedPlayersPane;

	private Timeline timeline;

	private SimpleLongProperty waitingToConnectGameId = new SimpleLongProperty();

	private MonoPublisher<None> destroyPublisher = Publisher.mono();

	@Override
	public void init(Object data) {
		subscribeToGameList();
		subscribeToConnectedPlayersList();

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

		waitingToConnectGameId.addListener((observableValue, number, gameId) -> {
			if (gameId.longValue() == -1) {
				waitingPlayersLabel.setVisible(false);
				cancelWaitButton.setVisible(false);
				cancelWaitButton.setDisable(true);
				gameItemsPane.getChildren();
				disable.set(false);
				timeline.stop();
			}
			else {
				waitingPlayersLabel.setVisible(true);
				cancelWaitButton.setVisible(true);
				cancelWaitButton.setDisable(false);
				disable.set(true);
				timeline.play();
			}
		});

		waitingToConnectGameId.set(-1);

		EventDispatcher.on(GameStartedEvent.class)
				.takeUntil(destroyPublisher.asObservable())
				.conditionalSubscribe(event -> {
					GamePlayDto gamePlayDto = event.getGamePlayDto();
					if (gamePlayDto.getGameId() == waitingToConnectGameId.get()) {
						UI.switchComponent(ComponentType.GAME, gamePlayDto);
						return false;
					}

					return true;
				});
	}

	@Override
	public void destroy() {
		if (timeline != null) {
			timeline.stop();
		}
		destroyPublisher.complete();
	}

	private void subscribeToGameList() {
		GAME_SERVICE.games().takeUntil(destroyPublisher.asObservable()).subscribe(gameViews -> {
			List<GameItem> gameItems = gameViews.stream().map(GameItem::new).collect(Collectors.toList());
			Collections.reverse(gameItems);
			gameItems.forEach(gameItem -> {
				gameItem.getJoinButton().disableProperty().bind(disable);
				gameItem.setOnJoinClick(event -> connectToGame(gameItem.getGameView().getId()));
				gameItem.setOnStartClick(event -> GAME_SERVICE.startGame(gameItem.getGameView().getId()));
			});
			Platform.runLater(() -> {
				ObservableList<Node> gameList = gameItemsPane.getChildren();
				gameList.clear();
				gameList.addAll(gameItems);
			});
		});
	}

	private void subscribeToConnectedPlayersList() {
		AUTHENTICATION_SERVICE.connectPlayers().takeUntil(destroyPublisher.asObservable()).subscribe(players -> {
			List<ConnectedPlayerItem> connectedPlayerItems = players.stream()
					.map(ConnectedPlayerItem::new)
					.collect(Collectors.toList());
			Collections.reverse(connectedPlayerItems);
			Platform.runLater(() -> {
				ObservableList<Node> playersNode = connectedPlayersPane.getChildren();
				playersNode.clear();
				playersNode.addAll(connectedPlayerItems);
			});
		});
	}

	private void connectToGame(long gameId) {
		disable.set(true);

		GAME_SERVICE.join(gameId).subscribe(answer -> {
			switch (answer) {
				case GAME_NOT_FOUND:
					Platform.runLater(() -> {
						Alert warning = new Alert(AlertType.WARNING);
						warning.setTitle("Warning");
						warning.setHeaderText("Game not found.");
						warning.setContentText("Game deleted or already completed.");
						warning.showAndWait();
					});
					disable.set(false);
					break;
				case ALREADY_FULL:
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Confirmation");
						alert.setHeaderText("Game is full.");
						alert.setContentText("Game already full (");
						alert.showAndWait();
					});
					disable.set(false);
					break;
				case ALREADY_STARTED:
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Confirmation");
						alert.setHeaderText("Game is started.");
						alert.setContentText("Game already started. Do you want to watch it?");

						Optional<ButtonType> result = alert.showAndWait();
						if (result.isPresent() && result.get() == ButtonType.OK) {
							GAME_SERVICE.watch(gameId)
									.subscribe(gameWatchDto -> UI.switchComponent(ComponentType.GAME, gameWatchDto));
						}
						disable.set(false);
					});
					break;
				case CONNECTED:
					waitingToConnectGameId.set(gameId);
					break;
				default:
					throw new IllegalStateException();
			}
		});
	}

	public void cancelWait() {
		GAME_SERVICE.leave(waitingToConnectGameId.get());
		waitingToConnectGameId.set(-1);
	}
}
