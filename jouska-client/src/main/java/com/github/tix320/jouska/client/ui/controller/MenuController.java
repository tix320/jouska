package com.github.tix320.jouska.client.ui.controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.infrastructure.UI.NotificationType;
import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import com.github.tix320.jouska.client.service.origin.AuthenticationOrigin;
import com.github.tix320.jouska.client.ui.controller.notification.NotificationController;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.LoopThread;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.util.Threads;
import javafx.animation.FadeTransition;
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

	private final MonoPublisher<None> destroyPublisher = Publisher.mono();

	private BlockingQueue<NotificationEvent<?, ?>> notificationsQueue;

	private final AuthenticationOrigin authenticationOrigin;

	public MenuController(AuthenticationOrigin authenticationOrigin) {
		this.authenticationOrigin = authenticationOrigin;
	}

	@Override
	public void init(Object data) {
		notificationsQueue = new LinkedBlockingQueue<>();

		changeContent(MenuContentType.LOBBY);
		loadingIndicator.visibleProperty().bind(loading);
		nicknameLabel.setText(CurrentUserContext.getPlayer().getNickname());
		EventDispatcher.on(MenuContentChangeEvent.class)
				.takeUntil(destroyPublisher.asObservable())
				.subscribe(event -> changeContent(event.getMenuContentType(), event.getData()));

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
			UI.normalize();
		});
	}

	public void logout() {
		CurrentUserContext.setPlayer(null);
		authenticationOrigin.logout();
		Configuration.updateCredentials("", "");
		UI.switchComponent(ComponentType.LOGIN);
	}

	private void runNotificationConsumer() {
		LoopThread loopThread = Threads.createLoopDaemonThread(() -> {
			NotificationEvent<?, ?> notificationEvent = notificationsQueue.take();
			NotificationType notificationType = notificationEvent.getNotificationType();
			Component component = UI.loadNotificationComponent(notificationType, notificationEvent);

			if (!(component.getController() instanceof NotificationController)) {
				throw new IllegalStateException("");
			}

			NotificationController<?> notificationController = (NotificationController<?>) component.getController();

			Parent content = component.getRoot();

			Platform.runLater(() -> {
				AnchorPane.setTopAnchor(content, 0.0);
				AnchorPane.setRightAnchor(content, 0.0);
				AnchorPane.setLeftAnchor(content, 0.0);
				AnchorPane.setBottomAnchor(content, 0.0);
				ObservableList<Node> children = this.notificationPane.getChildren();
				children.add(content);

				notificationPane.setDisable(true);

				FadeTransition translateTransition = new FadeTransition(Duration.seconds(0.3), notificationPane);

				translateTransition.setFromValue(0);
				translateTransition.setToValue(1);

				translateTransition.setOnFinished(event -> notificationPane.setDisable(false));

				translateTransition.play();
			});

			try {
				notificationEvent.onResolve().await(java.time.Duration.ofSeconds(30));
			}
			catch (TimeoutException ignored) {
				System.out.println("Notification skipped in Menu");
			}
			finally {
				MonoPublisher<None> onDestroy = Publisher.mono();
				Platform.runLater(() -> {
					FadeTransition translateTransition = new FadeTransition(Duration.seconds(0.1), notificationPane);

					notificationPane.setDisable(true);

					translateTransition.setFromValue(1);
					translateTransition.setToValue(0);
					translateTransition.play();

					translateTransition.setOnFinished(event -> {
						notificationPane.getChildren().clear();
						notificationController.destroy();
						onDestroy.publish(None.SELF);
					});

					translateTransition.play();
				});

				onDestroy.asObservable().await(java.time.Duration.ofSeconds(5));
			}
		});
		loopThread.start();
		destroyPublisher.asObservable().subscribe(none -> loopThread.stop());
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
