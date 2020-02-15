package com.github.tix320.jouska.client.ui;

import com.github.tix320.jouska.client.app.JouskaUI;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
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

		IntegerBinding turnDurationBinding = Bindings.createIntegerBinding(() -> {
			String text = turnDurationInput.getText();
			return text.isEmpty() ? 0 : Integer.parseInt(text);
		}, turnDurationInput.textProperty());
		IntegerBinding gameDurationBinding = Bindings.createIntegerBinding(() -> {
			String text = gameDurationInput.getText();
			return text.isEmpty() ? 0 : Integer.parseInt(text);
		}, gameDurationInput.textProperty());

		createButton.disableProperty()
				.bind(loading.or(gameNameTextField.textProperty().isEmpty())
						.or(turnDurationInput.textProperty().isEmpty())
						.or(turnDurationBinding.lessThan(5))
						.or(gameDurationInput.textProperty().isEmpty())
						.or(gameDurationBinding.lessThan(5)));
		playersCountChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
		playersCountChoice.setValue(2);

		makeTextFieldNumeric(turnDurationInput);
		makeTextFieldNumeric(gameDurationInput);

		turnDurationInput.setText("20");
		gameDurationInput.setText("20");
	}

	@FXML
	void create() {
		loading.set(true);
		GAME_SERVICE.create(new CreateGameCommand(gameNameTextField.getText(), playersCountChoice.getValue(),
				Integer.parseInt(turnDurationInput.getText()), Integer.parseInt(gameDurationInput.getText())))
				.subscribe(gameId -> JouskaUI.switchScene("game-joining"));
	}

	public void makeTextFieldNumeric(TextField textField) {
		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				textField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
	}

	public void back() {
		JouskaUI.switchScene("menu");
	}
}
