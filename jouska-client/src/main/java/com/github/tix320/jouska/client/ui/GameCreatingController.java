package com.github.tix320.jouska.client.ui;

import com.github.tix320.jouska.client.app.JouskaUI;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import static com.github.tix320.jouska.client.app.Services.GAME_SERVICE;

public class GameCreatingController implements Controller<Object> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private TextField gameNameTextField;

	@FXML
	private TextField turnDurationInput;

	@FXML
	private TextField gameDurationInput;

	@FXML
	private ChoiceBox<Integer> playersCountChoice;

	@FXML
	private Button createButton;

	@Override
	public void initialize(Object data) {
		gameNameTextField.disableProperty().bind(loading);
		turnDurationInput.disableProperty().bind(loading);
		gameDurationInput.disableProperty().bind(loading);
		playersCountChoice.disableProperty().bind(loading);
		createButton.disableProperty()
				.bind(loading.or(gameNameTextField.textProperty().isEmpty())
						.or(turnDurationInput.textProperty().isEmpty())
						.or(turnDurationInput.textProperty().isEqualTo("0"))
						.or(gameDurationInput.textProperty().isEmpty())
						.or(gameDurationInput.textProperty().isEqualTo("0")));
		playersCountChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
		playersCountChoice.setValue(2);

		makeTextFieldNumeric(turnDurationInput);
		makeTextFieldNumeric(gameDurationInput);

		turnDurationInput.setText("20");
		gameDurationInput.setText("20");
	}

	@FXML
	void create(ActionEvent event) {
		loading.set(true);
		GAME_SERVICE.create(new CreateGameCommand(gameNameTextField.getText(), playersCountChoice.getValue(),
				Integer.parseInt(turnDurationInput.getText()), Integer.parseInt(gameDurationInput.getText())))
				.subscribe(gameId -> {
					JouskaUI.switchScene("game-joining");
				});
	}

	public void makeTextFieldNumeric(TextField textField) {
		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				textField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
	}

	public void back(ActionEvent actionEvent) {
		JouskaUI.switchScene("menu");
	}
}
