package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.net.URL;

import com.github.tix320.jouska.client.ui.Controller;
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
			stage.setResizable(false);
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
			switchCompletePublisher.publish(None.SELF);
			switchCompletePublisher.complete();
		});

		return switchCompletePublisher.asObservable().toMono();
	}

	public static void close() {
		onExit.publish(None.SELF);
	}

	public static MonoObservable<None> onExit() {
		return onExit.asObservable().toMono();
	}
}
