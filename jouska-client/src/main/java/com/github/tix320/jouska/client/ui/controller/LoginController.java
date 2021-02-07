package com.github.tix320.jouska.client.ui.controller;

import java.util.Optional;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.service.origin.AuthenticationOrigin;
import com.github.tix320.jouska.core.dto.Credentials;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

public class LoginController implements Controller<Credentials> {

	@FXML
	private TextField nicknameInput;

	@FXML
	private PasswordField passwordInput;

	@FXML
	private Button loginButton;

	@FXML
	private Label errorLabel;

	private final Configuration configuration;

	private final AuthenticationOrigin authenticationOrigin;

	public LoginController(Configuration configuration, AuthenticationOrigin authenticationOrigin) {
		this.configuration = configuration;
		this.authenticationOrigin = authenticationOrigin;
	}

	@Override
	public void init(Credentials credentials) {
		if (credentials != null) {
			nicknameInput.setText(credentials.getNickname());
			passwordInput.setText(credentials.getPassword());
		}
		loginButton.disableProperty()
				.bind(nicknameInput.textProperty().isEmpty().or(passwordInput.textProperty().isEmpty()));
	}

	@Override
	public void destroy() {

	}

	public void login() {
		Credentials credentials = new Credentials(nicknameInput.getText(), passwordInput.getText());
		authenticationOrigin.login(credentials).subscribe(loginAnswer -> {
			switch (loginAnswer.getLoginResult()) {
				case SUCCESS -> {
					configuration.updateCredentials(nicknameInput.getText(), passwordInput.getText());
					CurrentUserContext.setPlayer(loginAnswer.getPlayer());
					UI.switchComponent(ComponentType.MENU);
				}
				case ALREADY_LOGGED -> Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Confirmation");
					alert.setHeaderText("Another logged session found.");
					alert.setContentText(String.format(
							"Dear %s. You are already logged with other session. Are you sure want to login now? Other session will be stopped.",
							loginAnswer.getPlayer().getNickname()));

					Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() == ButtonType.OK) {
						authenticationOrigin.forceLogin(credentials).subscribe(answer -> {
							switch (answer.getLoginResult()) {
								case SUCCESS -> {
									CurrentUserContext.setPlayer(answer.getPlayer());
									UI.switchComponent(ComponentType.MENU);
								}
								case INVALID_CREDENTIALS -> showError("Invalid username/password");
								default -> throw new IllegalStateException();
							}
						});
					} else {
						UI.switchComponent(ComponentType.LOGIN, credentials);
					}
				});
				case INVALID_CREDENTIALS -> showError("Invalid username/password");
				default -> throw new IllegalStateException();
			}
		});
	}

	public void registerNewAccount() {
		UI.switchComponent(ComponentType.REGISTRATION);
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
