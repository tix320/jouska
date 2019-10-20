package com.gitlab.tixtix320.jouska.client.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import static com.gitlab.tixtix320.jouska.client.app.Services.CLONDER;
import static com.gitlab.tixtix320.jouska.client.app.Services.GAME_SERVICE;


public class Main extends Application {

    public static void main(String[] args) {
//        Services.initialize("localhost", 8888);
//        CLONDER.registerTopicPublisher("pizdec", new TypeReference<GameBoard>() {
//        }).asObservable().subscribe(gameBoard -> {
//            System.out.println(gameBoard);
//        });
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
