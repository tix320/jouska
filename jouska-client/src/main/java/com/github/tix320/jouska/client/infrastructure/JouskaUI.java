package com.github.tix320.jouska.client.infrastructure;

import java.io.IOException;
import java.net.URL;

import com.github.tix320.jouska.client.app.Version;
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

public final class JouskaUI {

	private static final Publisher<None> onExit = Publisher.buffered(1);

	public static Stage stage;

	public static void initialize(Stage stage) {
		if (JouskaUI.stage == null) {
			JouskaUI.stage = stage;
			stage.getIcons().add(new Image(JouskaUI.class.getResourceAsStream("/installer.ico")));
			stage.setTitle("Jouska " + Version.VERSION);
		}
		else {
			throw new IllegalStateException("Application already initialized");
		}
	}

	public static MonoObservable<None> switchScene(ComponentType componentType) {
		return switchScene(componentType, null);
	}

	public static MonoObservable<None> switchScene(ComponentType componentType, Object data) {
		Parent root = loadFxml(componentType, data);

		Scene scene = new Scene(root);

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

	public static Parent loadFxml(ComponentType componentType, Object data) {
		Parent root;
		try {
			String resourceUrl = componentType.fxmlPath;
			URL resource = JouskaUI.class.getResource(resourceUrl);
			if (resource == null) {
				throw new IllegalArgumentException(String.format("Fxml %s not found", resourceUrl));
			}
			FXMLLoader loader = new FXMLLoader(resource);
			root = loader.load();
			Controller<Object> controller = loader.getController();
			controller.initialize(data);
			return root;
		}
		catch (IOException e) {
			throw new IllegalArgumentException(String.format("Scene %s not found", componentType), e);
		}

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
		GAME("/ui/game/game.fxml");

		private final String fxmlPath;

		ComponentType(String fxmlPath) {
			this.fxmlPath = fxmlPath;
		}
	}
}
