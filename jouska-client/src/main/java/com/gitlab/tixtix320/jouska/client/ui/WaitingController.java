package com.gitlab.tixtix320.jouska.client.ui;

import com.gitlab.tixtix320.jouska.client.app.Services;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import ui.model.GameInfo;

public class WaitingController implements Controller {

    private final Scene scene;

    private final AnchorPane root;

    private final ProgressIndicator progressIndicator;

    private final GameInfo gameInfo;

    public WaitingController(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
        progressIndicator = new ProgressIndicator();
        root = new AnchorPane();
        root.getChildren().add(progressIndicator);
        scene = new Scene(root);
    }

    @Override
    public Scene getOwnScene() {
        return scene;
    }

    @Override
    public void initialize() {
        Services.GAME_INFO_SERVICE.connect(gameInfo.getId()).subscribe(connected -> {
            if (connected) {
                Meduzon.switchController(new GameController(gameInfo.getPlayers(), 7, 7));
                System.out.println("connected");
            } else {
                Meduzon.switchController(new GameJoiningController());
            }
        });
    }
}
