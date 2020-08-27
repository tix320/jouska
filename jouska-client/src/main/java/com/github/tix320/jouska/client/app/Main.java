package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.service.origin.ApplicationUpdateOrigin;
import com.github.tix320.jouska.client.service.origin.AuthenticationOrigin;
import com.github.tix320.jouska.core.dto.Credentials;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import static com.github.tix320.jouska.client.app.AppConfig.INJECTOR;


public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
	}

	@Override
	public void start(Stage stage) {
		AppConfig.initialize(Configuration.getServerHost(), Configuration.getServerPort());
		UI.initialize(stage);
		UI.switchComponent(ComponentType.SERVER_CONNECT).subscribe(none -> {
			Platform.runLater(stage::show);

			try {
				AppConfig.connect();
				ApplicationUpdateOrigin applicationUpdateOrigin = INJECTOR.inject(ApplicationUpdateOrigin.class);
				applicationUpdateOrigin.checkUpdate(Version.VERSION, Version.os).subscribe(lastVersion -> {
					if (!lastVersion.equals("")) { // update
						UI.switchComponent(ComponentType.UPDATE_APP, lastVersion);
					}
					else {
						authenticate();
					}
				});
			}
			catch (Exception e) {
				e.printStackTrace();
				StringWriter out = new StringWriter();
				PrintWriter stringWriter = new PrintWriter(out);
				e.printStackTrace(stringWriter);
				UI.switchComponent(ComponentType.ERROR, out.toString());
			}
		});
	}

	@Override
	public void stop() throws IOException {
		AppConfig.stop();
	}

	private static void authenticate() {
		AuthenticationOrigin authenticationOrigin = INJECTOR.inject(AuthenticationOrigin.class);
		Credentials credentials = new Credentials(Configuration.getNickname(), Configuration.getPassword());
		authenticationOrigin.login(credentials).subscribe(loginAnswer -> {
			switch (loginAnswer.getLoginResult()) {
				case SUCCESS:
					CurrentUserContext.setPlayer(loginAnswer.getPlayer());
					UI.switchComponent(ComponentType.MENU);
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
							authenticationOrigin.forceLogin(credentials).subscribe(answer -> {
								switch (answer.getLoginResult()) {
									case SUCCESS:
										UI.switchComponent(ComponentType.MENU);
										CurrentUserContext.setPlayer(answer.getPlayer());
										break;
									case INVALID_CREDENTIALS:
										UI.switchComponent(ComponentType.LOGIN);
										break;
									default:
										throw new IllegalStateException();
								}
							});
						}
						else {
							UI.switchComponent(ComponentType.LOGIN, credentials);
						}
					});
					break;
				case INVALID_CREDENTIALS:
					UI.switchComponent(ComponentType.LOGIN);
					break;
				default:
					throw new IllegalStateException();
			}
		});
	}
}
