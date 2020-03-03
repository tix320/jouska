package com.github.tix320.jouska.client.infrastructure;

import java.io.IOException;
import java.net.URL;

import com.github.tix320.jouska.client.app.Version;
import com.github.tix320.jouska.client.ui.controller.Controller;
import com.github.tix320.jouska.client.ui.controller.MenuController;
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

	private static String currentMenuScene;

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

	public static MonoObservable<None> switchScene(String name) {
		return switchScene(name, null);
	}

	public static MonoObservable<None> switchScene(String name, Object data) {
		Parent root;
		try {
			FXMLLoader loader = new FXMLLoader(
					JouskaUI.class.getResource("/ui/{name}/{name}.fxml".replace("{name}", name)));
			root = loader.load();
			Controller<Object> controller = loader.getController();
			controller.initialize(data);
		}
		catch (IOException e) {
			throw new IllegalArgumentException(String.format("Scene %s not found", name), e);
		}

		Scene scene = new Scene(root);
		URL cssResource = JouskaUI.class.getResource(
				String.format("/ui/{name}/style.css".replace("{name}", name), name));
		if (cssResource != null) {
			scene.getStylesheets().add(cssResource.toExternalForm());
		}

		Publisher<None> switchCompletePublisher = Publisher.buffered(1);

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

	public static void changeMenuScene(String sceneName) {
		if (sceneName.equals(currentMenuScene)) {
			return;
		}
		Parent content = loadFxml(sceneName, null);
		MenuController.SELF.changeContent(content);
		currentMenuScene = sceneName;
	}

	public static Parent loadFxml(String name, Object data) {
		Parent root;
		try {
			String resourceUrl = "/ui/{name}/{name}.fxml".replace("{name}", name);
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
			throw new IllegalArgumentException(String.format("Scene %s not found", name), e);
		}

	}

	public static void close() {
		onExit.publish(None.SELF);
	}

	public static MonoObservable<None> onExit() {
		return onExit.asObservable().toMono();
	}

	public enum SceneType {
		MENU,
		GAME
	}
}
