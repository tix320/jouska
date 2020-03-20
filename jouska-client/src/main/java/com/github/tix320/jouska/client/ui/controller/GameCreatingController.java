package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.event.EventDispatcher;
import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.model.BoardType;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.GameType;
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
	private TextField gameNameInput;

	@FXML
	private TextField turnDurationInput;

	@FXML
	private TextField gameDurationInput;

	@FXML
	private ChoiceBox<Integer> playersCountChoice;

	@FXML
	private Button createButton;

	@Override
	public void init(Object data) {
		gameNameInput.disableProperty().bind(loading);
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
				.bind(loading.or(gameNameInput.textProperty().isEmpty())
						.or(turnDurationInput.textProperty().isEmpty())
						.or(turnDurationBinding.lessThan(1))
						.or(gameDurationInput.textProperty().isEmpty())
						.or(gameDurationBinding.lessThan(1)));
		playersCountChoice.setItems(FXCollections.observableArrayList(1, 2, 3, 4));
		playersCountChoice.setValue(2);

		makeTextFieldNumeric(turnDurationInput);
		makeTextFieldNumeric(gameDurationInput);

		turnDurationInput.setText("20");
		gameDurationInput.setText("20");
	}

	@Override
	public void destroy() {

	}

	@FXML
	void create() {
		loading.set(true);
		GAME_SERVICE.create(new CreateGameCommand(
				new GameSettings(gameNameInput.getText(), GameType.TIMED, BoardType.STANDARD,
						playersCountChoice.getValue(), Integer.parseInt(turnDurationInput.getText()),
						Integer.parseInt(gameDurationInput.getText()))))
				.subscribe(gameId -> EventDispatcher.fire(new MenuContentChangeEvent(MenuContentType.LOBBY)));
	}

	public void makeTextFieldNumeric(TextField textField) {
		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				textField.setText(newValue.replaceAll("[^\\d]", ""));
			}
		});
	}
}
