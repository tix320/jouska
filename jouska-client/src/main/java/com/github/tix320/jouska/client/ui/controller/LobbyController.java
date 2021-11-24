package com.github.tix320.jouska.client.ui.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.service.origin.AuthenticationOrigin;
import com.github.tix320.jouska.client.service.origin.ClientGameManagementOrigin;
import com.github.tix320.jouska.client.service.origin.ClientTournamentOrigin;
import com.github.tix320.jouska.client.ui.lobby.ConnectedPlayerItem;
import com.github.tix320.jouska.client.ui.lobby.GameItem;
import com.github.tix320.jouska.core.application.game.GameState;
import com.github.tix320.jouska.core.dto.GameListFilter;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.publisher.MonoPublisher;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.skimp.api.object.None;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class LobbyController implements Controller<Object> {

	private final SimpleBooleanProperty disable = new SimpleBooleanProperty(false);

	@FXML
	private FlowPane gameItemsPane;

	@FXML
	private ChoiceBox<TournamentView> tournamentFilter;

	@FXML
	private CheckBox completedCheckBox;

	@FXML
	private Label waitingPlayersLabel;

	@FXML
	private Button cancelWaitButton;

	@FXML
	private VBox connectedPlayersPane;

	private final TournamentView ALL = new TournamentView("", "Tournament", -1, -1, null, false);

	private Timeline timeline;

	private final SimpleStringProperty waitingToConnectGameId = new SimpleStringProperty("");

	private final AtomicReference<Subscription> gamesListSubscription = new AtomicReference<>();

	private final MonoPublisher<None> destroyPublisher = Publisher.mono();

	private final ClientGameManagementOrigin gameManagementOrigin;

	private final ClientTournamentOrigin tournamentOrigin;

	private final AuthenticationOrigin authenticationOrigin;

	public LobbyController(ClientGameManagementOrigin gameManagementOrigin, ClientTournamentOrigin tournamentOrigin,
						   AuthenticationOrigin authenticationOrigin) {
		this.gameManagementOrigin = gameManagementOrigin;
		this.tournamentOrigin = tournamentOrigin;
		this.authenticationOrigin = authenticationOrigin;
	}

	@Override
	public void init(Object data) {
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
			if (gameId == null) {
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

		waitingToConnectGameId.set(null);

		tournamentFilter.setConverter(new StringConverter<>() {
			@Override
			public String toString(TournamentView object) {
				return object.getName();
			}

			@Override
			public TournamentView fromString(String string) {
				return null;
			}
		});

		tournamentFilter.setValue(ALL);

		tournamentFilter.valueProperty()
				.addListener((observable, oldValue, selectedTournament) -> subscribeWithCurrentFilter());
		completedCheckBox.selectedProperty()
				.addListener((observable, oldValue, selected) -> subscribeWithCurrentFilter());

		subscribeToTournamentList();
		subscribeToConnectedPlayersList();
		subscribeWithCurrentFilter();
	}

	@Override
	public void destroy() {
		if (timeline != null) {
			timeline.stop();
		}
		destroyPublisher.complete();
	}

	private void subscribeWithCurrentFilter() {
		String tournamentId = null;
		TournamentView value = tournamentFilter.getValue();
		if (value != ALL) {
			tournamentId = value.getId();
		}

		boolean selected = completedCheckBox.isSelected();

		Set<GameState> states = new HashSet<>();
		states.add(GameState.INITIAL);
		states.add(GameState.RUNNING);
		if (selected) {
			states.add(GameState.COMPLETED);
		}

		subscribeToGameList(new GameListFilter(tournamentId, states));
	}

	private void subscribeToGameList(GameListFilter gameListFilter) {
		Subscription subscription = gamesListSubscription.get();
		if (subscription != null) {
			subscription.cancel();
		}

		gameManagementOrigin.games(gameListFilter).takeUntil(destroyPublisher.asObservable()).subscribe(sub -> {
			gamesListSubscription.set(sub);
			sub.request(Long.MAX_VALUE);
		}, gameViews -> {
			List<GameItem> gameItems = gameViews.stream().map(GameItem::new).collect(Collectors.toList());
			gameItems.forEach(gameItem -> {
				gameItem.disableJoinButtonOn(disable);
				gameItem.setOnJoinClick(event -> joinGame(gameItem.getGameView().getId()));
				gameItem.setOnWatchClick(event -> watchGame(gameItem.getGameView().getId()));
				gameItem.setOnStartClick(event -> gameManagementOrigin.startGame(gameItem.getGameView().getId()));
			});
			Platform.runLater(() -> {
				ObservableList<Node> gameList = gameItemsPane.getChildren();
				gameList.clear();
				gameList.addAll(gameItems);
			});
		});
	}

	private void subscribeToTournamentList() {
		tournamentOrigin.getTournaments()
				.takeUntil(destroyPublisher.asObservable())
				.subscribe(tournamentViews -> Platform.runLater(() -> {
					ObservableList<TournamentView> items = FXCollections.observableArrayList(tournamentViews);
					items.add(ALL);
					tournamentFilter.setItems(items);
				}));
	}

	private void subscribeToConnectedPlayersList() {
		authenticationOrigin.connectPlayers().takeUntil(destroyPublisher.asObservable()).subscribe(players -> {
			List<ConnectedPlayerItem> connectedPlayerItems = players.stream()
					.map(ConnectedPlayerItem::new)
					.collect(Collectors.toList());
			Platform.runLater(() -> {
				ObservableList<Node> playersNode = connectedPlayersPane.getChildren();
				playersNode.clear();
				playersNode.addAll(connectedPlayerItems);
			});
		});
	}

	private void joinGame(String gameId) {
		disable.set(true);

		gameManagementOrigin.join(gameId).subscribe(answer -> {
			switch (answer) {
				case ALREADY_FULL -> {
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Warning");
						alert.setHeaderText("Game is full.");
						alert.setContentText("Game already full ((");
						alert.showAndWait();
					});
					disable.set(false);
				}
				case ALREADY_STARTED -> {
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Warning");
						alert.setHeaderText("Game is started.");
						alert.setContentText("Game already started ((");
						alert.showAndWait();
					});
					disable.set(false);
				}
				case CONNECTED -> waitingToConnectGameId.set(gameId);
				default -> throw new IllegalStateException();
			}
		});
	}

	private void watchGame(String gameId) {
		gameManagementOrigin.watch(gameId)
				.subscribe(gameWatchDto -> UI.switchComponent(GameController.class, gameWatchDto));
	}

	public void cancelWait() {
		String id = waitingToConnectGameId.get();
		if (id != null) {
			gameManagementOrigin.leave(id);
			waitingToConnectGameId.set(null);
		}
	}
}
