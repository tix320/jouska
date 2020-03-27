package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.client.ui.helper.component.NumberTextField;
import com.github.tix320.jouska.client.ui.helper.component.TextFields;
import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
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
	private NumberTextField turnDurationInput;

	@FXML
	private NumberTextField playerTurnTotalDurationInput;

	@FXML
	private NumberTextField gameDurationInput;

	@FXML
	private ChoiceBox<Integer> playersCountChoice;

	@FXML
	private ChoiceBox<String> turnTotalDurationTypeChoice;


	@FXML
	private Button createButton;

	@Override
	public void init(Object data) {
		createButton.disableProperty()
				.bind(loading.or(gameNameInput.textProperty().isEmpty())
						.or(turnDurationInput.numberProperty().lessThan(1))
						.or(playerTurnTotalDurationInput.numberProperty().lessThan(1))
						.or(gameDurationInput.numberProperty().lessThan(1)));

		playersCountChoice.setItems(FXCollections.observableArrayList(2, 3, 4));
		playersCountChoice.setValue(2);
		turnTotalDurationTypeChoice.setItems(FXCollections.observableArrayList("seconds", "minutes"));
		turnTotalDurationTypeChoice.setValue("minutes");

		TextFields.makeNumeric(turnDurationInput);
		TextFields.makeNumeric(playerTurnTotalDurationInput);
		TextFields.makeNumeric(gameDurationInput);

		turnDurationInput.setNumber(20);
		playerTurnTotalDurationInput.setNumber(10);
		gameDurationInput.setNumber(20);
	}

	@Override
	public void destroy() {

	}

	@FXML
	void create() {
		loading.set(true);
		String durationType = turnTotalDurationTypeChoice.getValue();
		int number = playerTurnTotalDurationInput.getNumber();
		int playerTurnTotalDurationSeconds = durationType.equals("seconds") ? number : number * 60;
		GAME_SERVICE.create(new CreateGameCommand(
				new TimedGameSettings(gameNameInput.getText(), BoardType.STANDARD, playersCountChoice.getValue(),
						turnDurationInput.getNumber(), playerTurnTotalDurationSeconds, gameDurationInput.getNumber())))
				.subscribe(Subscriber.<Long>builder().onPublish(
						gameId -> EventDispatcher.fire(new MenuContentChangeEvent(MenuContentType.LOBBY)))
						.onError(throwable -> {
							throwable.printStackTrace();
							loading.set(false);
						}));
	}
}
