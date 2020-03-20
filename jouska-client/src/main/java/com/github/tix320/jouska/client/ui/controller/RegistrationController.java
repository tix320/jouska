package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.client.ui.helper.component.PastePreventTextField;
import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.jouska.core.dto.RegistrationCommand;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.AUTHENTICATION_SERVICE;

public class RegistrationController implements Controller<Object> {

	@FXML
	private TextField nicknameInput;

	@FXML
	private PasswordField passwordInput;

	@FXML
	private PastePreventTextField confirmPasswordInput;

	@FXML
	private Button registerButton;

	@FXML
	private Label errorLabel;

	@Override
	public void init(Object data) {
		registerButton.disableProperty()
				.bind(nicknameInput.textProperty().isEmpty().or(passwordInput.textProperty().isEmpty()));
	}

	@Override
	public void destroy() {

	}

	public void register() {
		if (!passwordInput.getText().equals(confirmPasswordInput.getText())) {
			showError("Passwords do not match");
			return;
		}
		AUTHENTICATION_SERVICE.register(new RegistrationCommand(nicknameInput.getText(), passwordInput.getText()))
				.subscribe(registrationAnswer -> {
					switch (registrationAnswer) {
						case SUCCESS:
							JouskaUI.switchComponent(ComponentType.LOGIN,
									new LoginCommand(nicknameInput.getText(), passwordInput.getText()));
							break;
						case NICKNAME_ALREADY_EXISTS:
							showError("Nickname already exists");
							break;
					}
				});
	}

	public void login() {
		JouskaUI.switchComponent(ComponentType.LOGIN);
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
