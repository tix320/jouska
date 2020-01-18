package com.gitlab.tixtix320.jouska.client.app;

import java.io.IOException;
import java.net.URL;

import com.gitlab.tixtix320.jouska.client.ui.Controller;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.subject.Subject;
import com.gitlab.tixtix320.kiwi.api.util.None;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Jouska {

	private static Stage stage;

	public static void initialize(Stage stage) {
		if (Jouska.stage == null) {
			Jouska.stage = stage;
			stage.setTitle("Jouska " + Version.VERSION);
			stage.setResizable(false);
		}
		else {
			throw new IllegalStateException("Application already initialized");
		}
	}

	public static Observable<None> switchScene(String name) {
		return switchScene(name, null);
	}

	public static Observable<None> switchScene(String name, Object data) {
		Parent root;
		try {
			FXMLLoader loader = new FXMLLoader(
					Jouska.class.getResource("/ui/{name}/{name}.fxml".replace("{name}", name)));
			root = loader.load();
			Controller<Object> controller = loader.getController();
			controller.initialize(data);
		}
		catch (IOException e) {
			throw new IllegalArgumentException(String.format("Scene %s not found", name), e);
		}

		Scene scene = new Scene(root);
		URL cssResource = Jouska.class.getResource(String.format("/ui/{name}/style.css".replace("{name}", name), name));
		if (cssResource != null) {
			scene.getStylesheets().add(cssResource.toExternalForm());
		}

		Subject<None> switchSubject = Subject.buffered(1);

		Platform.runLater(() -> {
			stage.setScene(scene);
			stage.sizeToScene();
			stage.centerOnScreen();
			switchSubject.next(None.SELF);
		});

		return switchSubject.asObservable();
	}
}
