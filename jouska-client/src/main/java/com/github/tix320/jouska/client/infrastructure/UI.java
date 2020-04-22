package com.github.tix320.jouska.client.infrastructure;

import java.io.IOException;
import java.net.URL;

import com.github.tix320.jouska.client.app.Version;
import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import com.github.tix320.jouska.client.ui.controller.Component;
import com.github.tix320.jouska.client.ui.controller.Controller;
import com.github.tix320.jouska.client.ui.controller.notification.GamePlayersOfflineNotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.GameStartSoonNotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.NotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.TournamentAcceptPlayerNotificationController;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class UI {

	private static final Publisher<None> onExit = Publisher.buffered(1);

	public static Stage stage;

	public static Component currentComponent;

	public static void initialize(Stage stage) {
		if (UI.stage == null) {
			UI.stage = stage;
			stage.getIcons().add(new Image(UI.class.getResourceAsStream("/installer.ico")));
			stage.setTitle("Jouska " + Version.VERSION);
		}
		else {
			throw new IllegalStateException("Application already initialized");
		}

		// TODO this is not place for this
		EventDispatcher.on(GameStartedEvent.class)
				.subscribe(event -> UI.switchComponent(ComponentType.GAME, event.getGamePlayDto()));
	}

	public static MonoObservable<None> switchComponent(ComponentType componentType) {
		return switchComponent(componentType, null);
	}

	public static MonoObservable<None> switchComponent(ComponentType componentType, Object data) {
		Component component = loadComponent(componentType, data);
		if (currentComponent != null) {
			currentComponent.getController().destroy();
		}
		currentComponent = component;
		Scene scene = new Scene(component.getRoot());

		Publisher<None> switchCompletePublisher = Publisher.single();

		Platform.runLater(() -> {
			stage.setScene(scene);
			normalize();
			stage.centerOnScreen();
			switchCompletePublisher.publish(None.SELF);
			switchCompletePublisher.complete();

		});

		return switchCompletePublisher.asObservable().toMono();
	}

	public static void normalize() {
		stage.sizeToScene();
		stage.setMinWidth(stage.getWidth());
		stage.setMinHeight(stage.getHeight());
	}

	public static Component loadComponent(ComponentType componentType, Object data) {
		FXMLLoader loader = loadFxml(componentType, null);
		Parent root = loader.getRoot();

		Controller<Object> controller = loader.getController();
		controller.init(data);
		return new ComponentImpl(root, controller);
	}

	public static Component loadNotificationComponent(NotificationType notificationType, NotificationEvent<?, ?> data) {
		ComponentType componentType = notificationType.componentType;

		@SuppressWarnings("unchecked")
		NotificationController<NotificationEvent<?, ?>> controller = (NotificationController<NotificationEvent<?, ?>>) Try
				.supplyOrRethrow(() -> notificationType.controllerClass.getConstructors()[0].newInstance());
		FXMLLoader fxmlLoader = loadFxml(componentType, controller);

		controller.init(data);
		return new ComponentImpl(fxmlLoader.getRoot(), controller);
	}

	private static FXMLLoader loadFxml(ComponentType componentType, Object controller) {
		String resourceUrl = componentType.fxmlPath;
		URL resource = UI.class.getResource(resourceUrl);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Fxml %s not found", resourceUrl));
		}
		FXMLLoader loader = new FXMLLoader(resource);
		if (controller != null) {
			loader.setController(controller);
		}
		try {
			loader.load();
		}
		catch (IOException e) {
			throw new IllegalArgumentException(String.format("Scene %s not found", componentType), e);
		}

		return loader;
	}

	public static void close() {
		onExit.publish(None.SELF);
	}

	public static MonoObservable<None> onExit() {
		return onExit.asObservable().toMono();
	}

	public enum ComponentType {
		SERVER_CONNECT("/ui/server-connect/server-connect.fxml"),
		UPDATE_APP("/ui/update-app/update-app.fxml"),
		LOGIN("/ui/auth/login.fxml"),
		REGISTRATION("/ui/auth/registration.fxml"),
		ERROR("/ui/error/error.fxml"),
		MENU("/ui/menu/menu.fxml"),
		CREATE_GAME("/ui/game-creating/game-creating.fxml"),
		LOBBY("/ui/lobby/lobby.fxml"),
		TOURNAMENT_LOBBY("/ui/tournament/tournament-lobby.fxml"),
		TOURNAMENT_CREATE("/ui/tournament/tournament-create.fxml"),
		TOURNAMENT_MANAGEMENT("/ui/tournament/tournament-view.fxml"),
		GAME("/ui/game/game.fxml"),
		CONFIRM_NOTIFICATION("/ui/menu/notification/confirm-notification.fxml"),
		WARNING_NOTIFICATION("/ui/menu/notification/warning-notification.fxml");

		private final String fxmlPath;

		ComponentType(String fxmlPath) {
			this.fxmlPath = fxmlPath;
		}
	}

	public enum NotificationType {
		TOURNAMENT_ACCEPT(ComponentType.CONFIRM_NOTIFICATION, TournamentAcceptPlayerNotificationController.class),
		GAME_PLAYERS_OFFLINE(ComponentType.WARNING_NOTIFICATION, GamePlayersOfflineNotificationController.class),
		GAME_START_SOON(ComponentType.CONFIRM_NOTIFICATION, GameStartSoonNotificationController.class);

		private final ComponentType componentType;

		private final Class<? extends NotificationController<?>> controllerClass;

		NotificationType(ComponentType componentType, Class<? extends NotificationController<?>> controllerClass) {
			this.componentType = componentType;
			this.controllerClass = controllerClass;
		}
	}

	private static final class ComponentImpl implements Component {

		private final Parent root;

		private final Controller<?> controller;

		private ComponentImpl(Parent root, Controller<?> controller) {
			this.root = root;
			this.controller = controller;
		}

		@Override
		public Parent getRoot() {
			return root;
		}

		@Override
		public Controller<?> getController() {
			return controller;
		}
	}
}
