package com.github.tix320.jouska.client.ui.controller;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.service.origin.ClientTournamentOrigin;
import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.client.ui.helper.component.NumberField;
import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.dto.ClassicTournamentSettingsDto;
import com.github.tix320.jouska.core.dto.CreateTournamentCommand;
import com.github.tix320.jouska.core.dto.SimpleGameSettingsDto;
import com.github.tix320.jouska.core.dto.TimedGameSettingsDto;
import com.github.tix320.jouska.core.event.EventDispatcher;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class TournamentCreateController implements Controller<Object> {

	@FXML
	private TextField gameNameInput;

	@FXML
	private NumberField groupTurnDurationInput;

	@FXML
	private NumberField groupTurnTotalDurationInput;

	@FXML
	private NumberField playOffTurnDurationInput;

	@FXML
	private NumberField playOffTurnTotalDurationInput;

	@FXML
	private ComboBox<Integer> playersCountChoice;

	@FXML
	private Button createButton;

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty isValid = new SimpleBooleanProperty(false);

	private final ClientTournamentOrigin tournamentOrigin;

	public TournamentCreateController(ClientTournamentOrigin tournamentOrigin) {
		this.tournamentOrigin = tournamentOrigin;
	}

	@Override
	public void init(Object data) {
		gameNameInput.disableProperty().bind(loading);
		groupTurnDurationInput.disableProperty().bind(loading);
		groupTurnTotalDurationInput.disableProperty().bind(loading);
		playOffTurnDurationInput.disableProperty().bind(loading);
		playOffTurnTotalDurationInput.disableProperty().bind(loading);
		playersCountChoice.disableProperty().bind(loading);

		initInputs();

		createButton.disableProperty().bind(loading.or(isValid.not()));
		playersCountChoice.setItems(Stream.iterate(2, i -> i + 1)
				.limit(15)
				.collect(Collectors.toCollection(FXCollections::observableArrayList)));
		playersCountChoice.setValue(16);

		groupTurnDurationInput.setNumber(20);
		groupTurnTotalDurationInput.setNumber(10);
		playOffTurnDurationInput.setNumber(20);
		playOffTurnTotalDurationInput.setNumber(10);
	}

	@Override
	public void destroy() {

	}

	private void initInputs() {
		isValid.bind(gameNameInput.textProperty()
				.isNotEmpty()
				.and(groupTurnDurationInput.numberProperty().greaterThan(0))
				.and(groupTurnTotalDurationInput.numberProperty().greaterThan(0))
				.and(playOffTurnDurationInput.numberProperty().greaterThan(0))
				.and(playOffTurnTotalDurationInput.numberProperty().greaterThan(0)));
	}

	@FXML
	private void create() {
		loading.set(true);
		TimedGameSettingsDto groupSettings = new TimedGameSettingsDto(
				new SimpleGameSettingsDto("None", BoardType.STANDARD, 2), groupTurnDurationInput.getNumber(),
				groupTurnTotalDurationInput.getNumber() * 60);
		TimedGameSettingsDto playOffSettings = new TimedGameSettingsDto(
				new SimpleGameSettingsDto("None", BoardType.STANDARD, 2), playOffTurnDurationInput.getNumber(),
				playOffTurnTotalDurationInput.getNumber() * 60);
		tournamentOrigin.create(new CreateTournamentCommand(
				new ClassicTournamentSettingsDto(gameNameInput.getText(), playersCountChoice.getValue(), groupSettings,
						playOffSettings))).subscribe(response -> {
			try {
				//noinspection ResultOfMethodCallIgnored
				response.get();
				EventDispatcher.fire(new MenuContentChangeEvent(MenuContentType.TOURNAMENT_LOBBY));
			} catch (Exception e) {
				loading.set(false);
				e.printStackTrace();
			}
		});
	}
}
