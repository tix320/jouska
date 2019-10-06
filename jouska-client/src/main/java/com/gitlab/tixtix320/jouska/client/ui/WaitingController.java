package com.gitlab.tixtix320.jouska.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitlab.tixtix320.jouska.client.app.Jouska;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import static com.gitlab.tixtix320.jouska.client.app.Services.CLONDER;

public class WaitingController implements Controller<Long> {

    @FXML
    private AnchorPane root;

    @FXML
    private Label infoLabel;

    @FXML
    private Button cancelButton;

    @Override
    public void initialize(Long gameId) {
        CLONDER.registerTopicPublisher("game-board: " + gameId, new TypeReference<GameBoard>() {
        }).asObservable().subscribe(gameBoard -> Platform.runLater(() -> Jouska.switchScene("game", gameBoard)));
    }

    @FXML
    void cancel(ActionEvent event) {

    }
}
