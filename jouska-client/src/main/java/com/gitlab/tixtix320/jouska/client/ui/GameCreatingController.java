package com.gitlab.tixtix320.jouska.client.ui;

import com.gitlab.tixtix320.jouska.client.app.Jouska;
import com.gitlab.tixtix320.jouska.core.dto.CreateGameCommand;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import static com.gitlab.tixtix320.jouska.client.app.Services.GAME_SERVICE;

public class GameCreatingController implements Controller<Object> {

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
		createButton.disableProperty().bind(loading);
		createButton.disableProperty().bind(gameNameTextField.textProperty().isEmpty());
		playersCountChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
		playersCountChoice.setValue(2);
	}

	@FXML
	void create(ActionEvent event) {
		loading.set(true);
		GAME_SERVICE.create(new CreateGameCommand(gameNameTextField.getText(), playersCountChoice.getValue()))
				.subscribe(gameId -> {
					Jouska.switchScene("game-joining");
				});
	}

	public void back(ActionEvent actionEvent) {
		Jouska.switchScene("menu");
	}
}
