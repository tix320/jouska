package com.github.tix320.jouska.client.ui.controller;

import java.util.Optional;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.core.dto.LoginCommand;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.AUTHENTICATION_SERVICE;

public class LoginController implements Controller<LoginCommand> {

	@FXML
	private TextField nicknameInput;

	@FXML
	private PasswordField passwordInput;

	@FXML
	private Button loginButton;

	@FXML
	private Label errorLabel;

	@Override
	public void init(LoginCommand loginCommand) {
		if (loginCommand != null) {
			nicknameInput.setText(loginCommand.getNickname());
			passwordInput.setText(loginCommand.getPassword());
		}
		loginButton.disableProperty()
				.bind(nicknameInput.textProperty().isEmpty().or(passwordInput.textProperty().isEmpty()));
	}

	@Override
	public void destroy() {

	}

	public void login() {
		LoginCommand loginCommand = new LoginCommand(nicknameInput.getText(), passwordInput.getText());
		AUTHENTICATION_SERVICE.login(loginCommand).subscribe(loginAnswer -> {
			switch (loginAnswer.getLoginResult()) {
				case SUCCESS:
					Configuration.updateCredentials(nicknameInput.getText(), passwordInput.getText());
					CurrentUserContext.setPlayer(loginAnswer.getPlayer());
					JouskaUI.switchComponent(ComponentType.MENU);
					break;
				case ALREADY_LOGGED:
					Platform.runLater(() -> {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Confirmation");
						alert.setHeaderText("Another logged session found.");
						alert.setContentText(String.format(
								"Dear %s. You are already logged with other session. Are you sure want to login now? Other session will be stopped.",
								loginAnswer.getPlayer().getNickname()));

						Optional<ButtonType> result = alert.showAndWait();
						if (result.isPresent() && result.get() == ButtonType.OK) {
							AUTHENTICATION_SERVICE.forceLogin(loginCommand).subscribe(answer -> {
								switch (answer.getLoginResult()) {
									case SUCCESS:
										CurrentUserContext.setPlayer(answer.getPlayer());
										JouskaUI.switchComponent(ComponentType.MENU);
										break;
									case INVALID_CREDENTIALS:
										showError("Invalid username/password");
										break;
									default:
										throw new IllegalStateException();
								}
							});
						}
						else {
							JouskaUI.switchComponent(ComponentType.LOGIN, loginCommand);
						}
					});
					break;
				case INVALID_CREDENTIALS:
					showError("Invalid username/password");
					break;
				default:
					throw new IllegalStateException();
			}
		});
	}

	public void registerNewAccount() {
		JouskaUI.switchComponent(ComponentType.REGISTRATION);
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
