package com.gitlab.tixtix320.jouska.client.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public final class MenuController implements Controller {

    private final Scene scene;

    private final AnchorPane root;

    private final Button gameJoiningButton;

    private final Button gameCreationButtonButton;

    public MenuController() {
        gameJoiningButton = createGameJoiningButton();
        gameCreationButtonButton = createGameCreationButton();
        root = new AnchorPane();
        root.getChildren().addAll(gameJoiningButton, gameCreationButtonButton);
        scene = new Scene(root, 300, 150);
    }

    public Button createGameJoiningButton() {
        Button button = new Button("Join game");
        button.setPrefWidth(150);
        button.setLayoutX(75);
        button.setLayoutY(25);
        button.setOnMouseClicked(event -> {
            Meduzon.switchController(new GameJoiningController());
        });
        return button;
    }

    public Button createGameCreationButton() {
        Button button = new Button("Create game");
        button.setPrefWidth(150);
        button.setLayoutX(75);
        button.setLayoutY(75);
        return button;
    }

    @Override
    public Scene getOwnScene() {
        return scene;
    }

    @Override
    public void initialize() {

    }
}
