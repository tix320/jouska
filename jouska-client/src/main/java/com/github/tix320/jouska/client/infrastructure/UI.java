package com.github.tix320.jouska.client.infrastructure;

import java.io.IOException;
import java.net.URL;

import com.github.tix320.jouska.client.app.Version;
import com.github.tix320.jouska.client.ui.controller.Component;
import com.github.tix320.jouska.client.ui.controller.Controller;
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
			stage.sizeToScene();
			stage.centerOnScreen();
			stage.setMinWidth(stage.getWidth());
			stage.setMinHeight(stage.getHeight());
			switchCompletePublisher.publish(None.SELF);
			switchCompletePublisher.complete();

		});

		return switchCompletePublisher.asObservable().toMono();
	}

	public static Component loadComponent(ComponentType componentType, Object data) {
		String resourceUrl = componentType.fxmlPath;
		URL resource = UI.class.getResource(resourceUrl);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Fxml %s not found", resourceUrl));
		}
		FXMLLoader loader = new FXMLLoader(resource);
		Parent root;
		try {
			root = loader.load();
		}
		catch (IOException e) {
			throw new IllegalArgumentException(String.format("Scene %s not found", componentType), e);
		}
		Controller<Object> controller = loader.getController();
		controller.init(data);
		return new ComponentImpl(root, controller);
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
		TOURNAMENT_MANAGEMENT("/ui/tournament/tournament-management.fxml"),
		GAME("/ui/game/game.fxml"),

		TOURNAMENT_ACCEPT_NOTIFICATION("/ui/tournament/accept-player-notification.fxml");

		private final String fxmlPath;

		ComponentType(String fxmlPath) {
			this.fxmlPath = fxmlPath;
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