package com.gitlab.tixtix320.jouska.client.app;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Jouska.initialize(stage);
    }

    public static void main(String[] args) {
        Services.initialize("localhost", 8888);
        launch(args);
    }
}
