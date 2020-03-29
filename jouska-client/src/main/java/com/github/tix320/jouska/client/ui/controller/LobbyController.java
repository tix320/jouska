package com.github.tix320.jouska.client.ui.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.client.ui.lobby.GameItem;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.GAME_SERVICE;

public class LobbyController implements Controller<Object> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private FlowPane gameItemsPane;

	@FXML
	private Label waitingPlayersLabel;

	private Timeline timeline;

	private AtomicReference<Long> waitingToConnectGameId = new AtomicReference<>(null);

	private MonoPublisher<None> destroyPublisher = Publisher.mono();

	@Override
	public void init(Object data) {
		gameItemsPane.disableProperty().bind(loading);
		subscribeToGameList();

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

		waitingPlayersLabel.visibleProperty().addListener((observable, oldValue, visible) -> {
			if (visible) {
				timeline.play();
			}
			else {
				timeline.stop();
			}
		});

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
			gameItems.forEach(gameItem -> gameItem.setOnMouseClicked(event -> onItemClick(gameItem, event)));
			Platform.runLater(() -> {
				ObservableList<Node> gameList = gameItemsPane.getChildren();
				gameList.clear();
				gameList.addAll(gameItems);
			});
		});
	}

	private void onItemClick(GameItem gameItem, MouseEvent mouseEvent) {
		if (mouseEvent.getClickCount() == 2) {
			GameView gameView = gameItem.getGameView();
			long gameId = gameView.getId();
			loading.set(true);

			waitingToConnectGameId.set(gameId);

			GAME_SERVICE.connect(gameId).subscribe(answer -> {
				switch (answer) {
					case GAME_NOT_FOUND:
						Platform.runLater(() -> {
							Alert warning = new Alert(AlertType.WARNING);
							warning.setTitle("Warning");
							warning.setHeaderText("Game not found.");
							warning.setContentText("Game deleted or already completed.");
							warning.showAndWait();
						});
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
										.subscribe(gameWatchDto -> UI.switchComponent(ComponentType.GAME,
												gameWatchDto));
							}
							else {
								loading.set(false);
							}
						});
						break;
					case CONNECTED:
						waitingPlayersLabel.setVisible(true);
						break;
					default:
						throw new IllegalStateException();
				}
			});
		}
	}
}
