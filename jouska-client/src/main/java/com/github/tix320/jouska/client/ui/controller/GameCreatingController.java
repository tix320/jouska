package com.github.tix320.jouska.client.ui.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.service.origin.ClientGameManagementOrigin;
import com.github.tix320.jouska.client.service.origin.ClientPlayerOrigin;
import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.client.ui.helper.component.NumberField;
import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.SimpleGameSettingsDto;
import com.github.tix320.jouska.core.dto.TimedGameSettingsDto;
import com.github.tix320.jouska.core.event.EventDispatcher;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

public class GameCreatingController implements Controller<Object> {

	@FXML
	private TextField gameNameInput;

	@FXML
	private NumberField turnDurationInput;

	@FXML
	private NumberField playerTurnTotalDurationInput;

	@FXML
	private ChoiceBox<Integer> playersCountChoice;

	@FXML
	private TextArea accessPlayersTextArea;

	@FXML
	private ChoiceBox<String> turnTotalDurationTypeChoice;

	@FXML
	private Button createButton;

	@FXML
	private Label errorLabel;

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);
	private final SimpleIntegerProperty turnDuration = new SimpleIntegerProperty(20);
	private final SimpleIntegerProperty playerTurnTotalDuration = new SimpleIntegerProperty(10);

	private final ClientPlayerOrigin playerOrigin;

	private final ClientGameManagementOrigin gameManagementOrigin;

	public GameCreatingController(ClientPlayerOrigin playerOrigin, ClientGameManagementOrigin gameManagementOrigin) {
		this.playerOrigin = playerOrigin;
		this.gameManagementOrigin = gameManagementOrigin;
	}

	@Override
	public void init(Object data) {
		createButton.disableProperty()
				.bind(loading.or(gameNameInput.textProperty().isEmpty())
						.or(turnDuration.lessThan(1))
						.or(playerTurnTotalDuration.lessThan(1)));

		turnDurationInput.numberProperty().bindBidirectional(turnDuration);
		playerTurnTotalDurationInput.numberProperty().bindBidirectional(playerTurnTotalDuration);

		playersCountChoice.setItems(FXCollections.observableArrayList(2, 3, 4));
		playersCountChoice.setValue(2);
		turnTotalDurationTypeChoice.setItems(FXCollections.observableArrayList("seconds", "minutes"));
		turnTotalDurationTypeChoice.setValue("minutes");
	}

	@Override
	public void destroy() {

	}

	@FXML
	void create() {
		loading.set(true);
		String durationType = turnTotalDurationTypeChoice.getValue();
		int number = playerTurnTotalDuration.get();
		int playerTurnTotalDurationSeconds = durationType.equals("seconds") ? number : number * 60;
		List<String> playerNicknames = Arrays.stream(accessPlayersTextArea.getText().strip().split(","))
				.map(String::strip)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());

		playerOrigin.getPlayersByNickname(playerNicknames).subscribe(players -> {
			StringJoiner nonExistingNicknamesJoiner = new StringJoiner(",", "[", "]");
			nonExistingNicknamesJoiner.setEmptyValue("");
			for (int i = 0; i < playerNicknames.size(); i++) {
				String nickname = playerNicknames.get(i);
				if (players.get(i) == null) {
					nonExistingNicknamesJoiner.add(nickname);
				}
			}

			String nonExistingNicknames = nonExistingNicknamesJoiner.toString();
			if (!nonExistingNicknames.isEmpty()) {
				showError(String.format("The following nicknames does not exists %s", nonExistingNicknames));
				loading.set(false);
			}
			else {
				gameManagementOrigin.create(new CreateGameCommand(new TimedGameSettingsDto(
						new SimpleGameSettingsDto(gameNameInput.getText(), BoardType.STANDARD,
								playersCountChoice.getValue()), turnDuration.get(), playerTurnTotalDurationSeconds),
						new HashSet<>(players))).subscribe(response -> {
					if (response.isSuccess()) {
						EventDispatcher.fire(new MenuContentChangeEvent(MenuContentType.LOBBY));
					}
					else {
						loading.set(false);
						response.getError().printStackTrace();
					}
				});
			}
		});
	}

	public void showError(String message) {
		Platform.runLater(() -> {
			errorLabel.setVisible(true);
			errorLabel.setText(message);
			FadeTransition transition = new FadeTransition(Duration.seconds(1), errorLabel);
			transition.setFromValue(0);
			transition.setToValue(1);
			transition.play();
		});
	}
}
