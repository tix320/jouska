package com.github.tix320.jouska.client.app;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.kiwi.api.check.Try;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import static com.github.tix320.jouska.client.app.Services.APPLICATION_INSTALLER_SERVICE;
import static com.github.tix320.jouska.client.app.Services.AUTHENTICATION_SERVICE;


public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
	}

	@Override
	public void start(Stage stage) {
		JouskaUI.initialize(stage);
		JouskaUI.switchScene(ComponentType.SERVER_CONNECT).subscribe(none -> {
			stage.show();

			new Thread(() -> {
				try {
					Services.initialize(Configuration.getServerHost(), Configuration.getServerPort());
					APPLICATION_INSTALLER_SERVICE.checkUpdate(Version.VERSION, Version.os.name())
							.subscribe(lastVersion -> {
								if (!lastVersion.equals("")) { // update
									JouskaUI.switchScene(ComponentType.UPDATE_APP, lastVersion);
								}
								else {
									LoginCommand loginCommand = new LoginCommand(Configuration.getNickname(),
											Configuration.getPassword());
									AUTHENTICATION_SERVICE.login(loginCommand).subscribe(loginAnswer -> {
										switch (loginAnswer) {
											case SUCCESS:
												JouskaUI.switchScene(ComponentType.MENU);
												break;
											case ALREADY_LOGGED:
												Platform.runLater(() -> {
													Alert alert = new Alert(AlertType.CONFIRMATION);
													alert.setTitle("Confirmation");
													alert.setHeaderText("Another logged session found.");
													alert.setContentText(
															"You are already logged with other session. Are you sure want to login now? Other session will be stopped.");

													Optional<ButtonType> result = alert.showAndWait();
													if (result.isPresent() && result.get() == ButtonType.OK) {
														AUTHENTICATION_SERVICE.forceLogin(loginCommand)
																.subscribe(answer -> {
																	switch (answer) {
																		case SUCCESS:
																			JouskaUI.switchScene(ComponentType.MENU);
																			break;
																		case INVALID_CREDENTIALS:
																			JouskaUI.switchScene(ComponentType.LOGIN);
																			break;
																		default:
																			throw new IllegalStateException();
																	}
																});
													}
													else {
														JouskaUI.switchScene(ComponentType.LOGIN, loginCommand);
													}
												});
												break;
											case INVALID_CREDENTIALS:
												JouskaUI.switchScene(ComponentType.LOGIN);
												break;
											default:
												throw new IllegalStateException();
										}
									});
								}
							});
				}
				catch (Exception e) {
					e.printStackTrace();
					StringWriter out = new StringWriter();
					PrintWriter stringWriter = new PrintWriter(out);
					e.printStackTrace(stringWriter);
					JouskaUI.switchScene(ComponentType.ERROR, out.toString());
				}
			}).start();
		});
	}

	@Override
	public void stop() {
		JouskaUI.close();
		AUTHENTICATION_SERVICE.logout()
				.subscribe(none -> Try.runOrRethrow(Services::stop), () -> Try.runOrRethrow(Services::stop));
	}
}
