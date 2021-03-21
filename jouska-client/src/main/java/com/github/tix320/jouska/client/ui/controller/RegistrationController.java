package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.service.origin.AuthenticationOrigin;
import com.github.tix320.jouska.client.ui.helper.component.PastePreventPasswordField;
import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.RegistrationCommand;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class RegistrationController implements Controller<Object> {

	@FXML
	private TextField nicknameInput;

	@FXML
	private PasswordField passwordInput;

	@FXML
	private PastePreventPasswordField confirmPasswordInput;

	@FXML
	private Button registerButton;

	@FXML
	private Label errorLabel;

	private final AuthenticationOrigin authenticationOrigin;

	public RegistrationController(AuthenticationOrigin authenticationOrigin) {
		this.authenticationOrigin = authenticationOrigin;
	}

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
		authenticationOrigin.register(new RegistrationCommand(nicknameInput.getText(), passwordInput.getText()))
				.subscribe(registrationAnswer -> {
					switch (registrationAnswer) {
						case SUCCESS -> UI.switchComponent(LoginController.class,
								new Credentials(nicknameInput.getText(), passwordInput.getText()));
						case NICKNAME_ALREADY_EXISTS -> showError("Nickname already exists");
					}
				});
	}

	public void login() {
		UI.switchComponent(LoginController.class);
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
