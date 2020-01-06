package com.gitlab.tixtix320.jouska.client.app;

import com.gitlab.tixtix320.jouska.client.ui.Controller;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public final class Jouska {

    private static Stage stage;

    public static void initialize(Stage stage) {
        if (Jouska.stage == null) {
            Jouska.stage = stage;
            stage.setTitle("Jouska");
            stage.setResizable(false);
            stage.sizeToScene();

        } else {
            throw new IllegalStateException("Application already initialized");
        }
    }

    public static void switchScene(String name) {
        switchScene(name, null);
    }

    public static void switchScene(String name, Object data) {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(Jouska.class.getResource("/ui/{name}/{name}.fxml".replace("{name}", name)));
            root = loader.load();
            Controller<Object> controller = loader.getController();
            controller.initialize(data);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("Scene %s not found", name), e);
        }

        Scene scene = new Scene(root);
        URL cssResource = Jouska.class.getResource(String.format("/ui/{name}/style.css".replace("{name}", name), name));
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        }
        Platform.runLater(() -> stage.setScene(scene));
    }
}
