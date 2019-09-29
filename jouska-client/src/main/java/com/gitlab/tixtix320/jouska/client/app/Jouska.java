package com.gitlab.tixtix320.jouska.client.app;

import javafx.stage.Stage;
import com.gitlab.tixtix320.jouska.client.ui.Controller;
import com.gitlab.tixtix320.jouska.client.ui.MenuController;

public final class Jouska {

    private static Stage stage;

    public static void initialize(Stage stage) {
        if (Jouska.stage == null) {
            Jouska.stage = stage;
            stage.setWidth(1280);
            stage.setHeight(720);
            stage.setTitle("Petrichor");
            stage.sizeToScene();
            stage.setResizable(false);
            stage.show();
            switchController(new MenuController());
        } else {
            throw new IllegalStateException("Application already initialized");
        }
    }

    public static void switchController(Controller controller) {
        stage.setScene(controller.getOwnScene());
        controller.initialize();
    }
}
