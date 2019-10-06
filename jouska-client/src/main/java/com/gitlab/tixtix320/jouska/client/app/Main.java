package com.gitlab.tixtix320.jouska.client.app;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        Services.initialize("localhost", 8888);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Jouska.initialize(stage);
    }

    @Override
    public void stop() throws Exception {
        Services.stop();
    }
}
