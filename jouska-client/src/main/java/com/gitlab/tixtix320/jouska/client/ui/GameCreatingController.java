package com.gitlab.tixtix320.jouska.client.ui;

import com.gitlab.tixtix320.jouska.client.app.Jouska;
import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import static com.gitlab.tixtix320.jouska.client.app.Services.GAME_SERVICE;

public class GameCreatingController implements Controller {

    private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

    @FXML
    private TextField gameNameTextField;

    @FXML
    private ChoiceBox<Integer> playersCountChoice;

    @FXML
    private Button createButton;

    @Override
    public void initialize(Object data) {
        gameNameTextField.disableProperty().bind(loading);
        playersCountChoice.disableProperty().bind(loading);
        createButton.disableProperty().bind(gameNameTextField.textProperty().isEmpty());
        playersCountChoice.setItems(FXCollections.observableArrayList(2, 3, 4));
        playersCountChoice.setValue(2);
    }

    @FXML
    void create(ActionEvent event) {
        loading.set(true);
        GameInfo gameInfo = new GameInfo(-1, gameNameTextField.getText(), -1, playersCountChoice.getValue());
        GAME_SERVICE.create(gameInfo).subscribe(gameId -> {
            GAME_SERVICE.connect(gameId).subscribe(status -> {
                if (status.equals("connected")) {
                    Platform.runLater(() -> Jouska.switchScene("waiting", gameId));
                } else {
                    // show popup error
                }
            });
        });
    }
}
