package com.github.tix320.jouska.client.ui.controller;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tix320.jouska.client.ui.controller.MenuController.ContentType;
import com.github.tix320.jouska.client.ui.helper.component.TextFields;
import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.model.BoardType;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.GameType;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.converter.NumberStringConverter;

import static com.github.tix320.jouska.client.app.Services.TOURNAMENT_SERVICE;

public class TournamentCreateController implements Controller<Object> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty validation = new SimpleBooleanProperty(false);

	private final SimpleIntegerProperty groupTurnDuration = new SimpleIntegerProperty(20);
	private final SimpleIntegerProperty groupGameDuration = new SimpleIntegerProperty(20);
	private final SimpleIntegerProperty playOffTurnDuration = new SimpleIntegerProperty(20);
	private final SimpleIntegerProperty playOffGameDuration = new SimpleIntegerProperty(20);

	@FXML
	private TextField gameNameInput;

	@FXML
	private ToggleGroup settingsGroup;

	@FXML
	private RadioButton groupRadioButton;

	@FXML
	private RadioButton playOffRadioButton;

	@FXML
	private TextField turnDurationInput;

	@FXML
	private TextField gameDurationInput;

	@FXML
	private ComboBox<Integer> playersCountChoice;

	@FXML
	private Button createButton;

	@Override
	public void initialize(Object data) {
		gameNameInput.disableProperty().bind(loading);
		turnDurationInput.disableProperty().bind(loading);
		gameDurationInput.disableProperty().bind(loading);
		playersCountChoice.disableProperty().bind(loading);

		initInputs();

		settingsGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == groupRadioButton) {
				Bindings.unbindBidirectional(turnDurationInput.textProperty(), playOffTurnDuration);
				Bindings.unbindBidirectional(gameDurationInput.textProperty(), playOffGameDuration);

				Bindings.bindBidirectional(turnDurationInput.textProperty(), groupTurnDuration,
						new NumberStringConverter());
				Bindings.bindBidirectional(gameDurationInput.textProperty(), groupGameDuration,
						new NumberStringConverter());
			}
			else if (newValue == playOffRadioButton) {
				Bindings.unbindBidirectional(turnDurationInput.textProperty(), groupTurnDuration);
				Bindings.unbindBidirectional(gameDurationInput.textProperty(), groupGameDuration);

				Bindings.bindBidirectional(turnDurationInput.textProperty(), playOffTurnDuration,
						new NumberStringConverter());
				Bindings.bindBidirectional(gameDurationInput.textProperty(), playOffGameDuration,
						new NumberStringConverter());
			}
			else {
				throw new IllegalArgumentException("wtf");
			}
		});

		settingsGroup.selectToggle(groupRadioButton);

		createButton.disableProperty().bind(loading.or(validation.not()));
		playersCountChoice.setItems(Stream.iterate(2, i -> i + 1)
				.limit(15)
				.collect(Collectors.toCollection(FXCollections::observableArrayList)));
		playersCountChoice.setValue(16);

		TextFields.makeNumeric(turnDurationInput);
		TextFields.makeNumeric(gameDurationInput);
	}

	private void initInputs() {
		IntegerBinding turnDurationBinding = Bindings.createIntegerBinding(() -> {
			String text = turnDurationInput.getText();
			return text.isEmpty() ? 0 : Integer.parseInt(text);
		}, turnDurationInput.textProperty());
		IntegerBinding gameDurationBinding = Bindings.createIntegerBinding(() -> {
			String text = gameDurationInput.getText();
			return text.isEmpty() ? 0 : Integer.parseInt(text);
		}, gameDurationInput.textProperty());

		validation.bind(gameNameInput.textProperty()
				.isNotEmpty()
				.and(turnDurationInput.textProperty().isNotEmpty())
				.and(turnDurationBinding.greaterThan(5))
				.and(gameDurationInput.textProperty().isNotEmpty())
				.and(gameDurationBinding.greaterThan(1)));
	}

	@FXML
	private void create() {
		loading.set(true);
		GameSettings groupSettings = new GameSettings(GameType.TIMED, BoardType.STANDARD, groupTurnDuration.get(),
				groupGameDuration.get());
		GameSettings playOffSettings = new GameSettings(GameType.TIMED, BoardType.STANDARD, playOffTurnDuration.get(),
				playOffGameDuration.get());
		TOURNAMENT_SERVICE.create(
				new CreateTournamentCommand(gameNameInput.getText(), playersCountChoice.getValue(), groupSettings,
						playOffSettings))
				.subscribe(gameId -> Platform.runLater(
						() -> MenuController.SELF.changeContent(ContentType.TOURNAMENT_LOBBY)));
	}
}
