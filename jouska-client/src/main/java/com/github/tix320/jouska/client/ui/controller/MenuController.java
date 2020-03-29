package com.github.tix320.jouska.client.ui.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.infrastructure.event.NotificationEvent;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.util.Threads;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.AUTHENTICATION_SERVICE;

public final class MenuController implements Controller<Object> {

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private Label nicknameLabel;

	@FXML
	private AnchorPane contentPane;

	@FXML
	private AnchorPane notificationPane;

	private MenuContentType currentMenuContentType;

	private Component currentContentComponent;

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	private MonoPublisher<None> destroyPublisher = Publisher.mono();

	private BlockingQueue<NotificationEvent> notificationsQueue;

	@Override
	public void init(Object data) {
		notificationsQueue = new LinkedBlockingQueue<>();

		changeContent(MenuContentType.LOBBY);
		loadingIndicator.visibleProperty().bind(loading);
		nicknameLabel.setText(CurrentUserContext.getPlayer().getNickname());
		EventDispatcher.on(MenuContentChangeEvent.class)
				.takeUntil(destroyPublisher.asObservable())
				.subscribe(event -> changeContent(event.getMenuContentType()));

		EventDispatcher.on(NotificationEvent.class)
				.takeUntil(destroyPublisher.asObservable())
				.subscribe(event -> notificationsQueue.add(event));

		runNotificationConsumer();
	}

	@Override
	public void destroy() {
		currentContentComponent.getController().destroy();
		destroyPublisher.complete();
	}

	@FXML
	void toLobby() {
		changeContent(MenuContentType.LOBBY);
	}

	@FXML
	void createGame() {
		changeContent(MenuContentType.CREATE_GAME);
	}

	@FXML
	public void toTournament() {
		changeContent(MenuContentType.TOURNAMENT_LOBBY);
	}

	private void changeContent(MenuContentType menuContentType) {
		changeContent(menuContentType, null);
	}

	public void changeContent(MenuContentType menuContentType, Object data) {
		if (menuContentType == currentMenuContentType) {
			return;
		}

		Component component = UI.loadComponent(menuContentType.getComponentType(), data);
		if (currentContentComponent != null) {
			currentContentComponent.getController().destroy();
		}

		Parent content = component.getRoot();
		Platform.runLater(() -> {
			AnchorPane.setTopAnchor(content, 0.0);
			AnchorPane.setRightAnchor(content, 0.0);
			AnchorPane.setLeftAnchor(content, 0.0);
			AnchorPane.setBottomAnchor(content, 0.0);
			ObservableList<Node> children = this.contentPane.getChildren();
			children.clear();
			children.add(content);

			currentMenuContentType = menuContentType;
			currentContentComponent = component;
		});
	}

	public void logout() {
		CurrentUserContext.setPlayer(null);
		AUTHENTICATION_SERVICE.logout();
		Configuration.updateCredentials("", "");
		UI.switchComponent(ComponentType.LOGIN);
	}

	private void runNotificationConsumer() {
		AtomicBoolean running = Threads.runLoop(() -> {
			// set timeout to avoid infinitely sleep in case, when queue won't be filled anymore
			NotificationEvent notificationEvent = Try.supplyOrRethrow(
					() -> notificationsQueue.poll(5, TimeUnit.SECONDS));
			if (notificationEvent == null) {
				return true;
			}
			ComponentType componentType = notificationEvent.getComponentType();
			Object data = notificationEvent.getData();
			Component component = UI.loadComponent(componentType, data);

			if (!(component.getController() instanceof NotificationController)) {
				throw new IllegalStateException("");
			}

			@SuppressWarnings("unchecked")
			NotificationController<Object> notificationController = (NotificationController<Object>) component.getController();

			Parent content = component.getRoot();

			Platform.runLater(() -> {
				AnchorPane.setTopAnchor(content, 0.0);
				AnchorPane.setRightAnchor(content, 0.0);
				AnchorPane.setLeftAnchor(content, 0.0);
				AnchorPane.setBottomAnchor(content, 0.0);
				ObservableList<Node> children = this.notificationPane.getChildren();
				children.clear();
				children.add(content);

				TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1),
						notificationPane);

				translateTransition.setFromY(-100);
				translateTransition.setToX(0);
				translateTransition.play();
			});

			try {
				notificationController.resolved().blockUntilComplete(java.time.Duration.ofSeconds(30));
			}
			catch (TimeoutException ignored) {

			}
			finally {
				Platform.runLater(() -> {
					TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1),
							notificationPane);

					translateTransition.setFromY(0);
					translateTransition.setToX(-100);

					translateTransition.setOnFinished(event -> {
						notificationPane.getChildren().clear();
						notificationController.destroy();
					});

					translateTransition.play();
				});
			}

			return true;
		});
		destroyPublisher.asObservable().subscribe(none -> running.set(false));
	}

	public enum MenuContentType {
		CREATE_GAME {
			@Override
			ComponentType getComponentType() {
				return ComponentType.CREATE_GAME;
			}
		},
		LOBBY {
			@Override
			ComponentType getComponentType() {
				return ComponentType.LOBBY;
			}
		},
		TOURNAMENT_LOBBY {
			@Override
			ComponentType getComponentType() {
				return ComponentType.TOURNAMENT_LOBBY;
			}
		},
		TOURNAMENT_CREATE {
			@Override
			ComponentType getComponentType() {
				return ComponentType.TOURNAMENT_CREATE;
			}
		},
		TOURNAMENT_MANAGEMENT {
			@Override
			ComponentType getComponentType() {
				return ComponentType.TOURNAMENT_MANAGEMENT;
			}
		};

		abstract ComponentType getComponentType();
	}
}
