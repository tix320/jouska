package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.kiwi.api.util.Threads;
import com.github.tix320.sonder.api.client.event.ConnectionClosedEvent;
import com.github.tix320.sonder.api.client.event.ConnectionEstablishedEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import static com.github.tix320.jouska.client.app.Services.*;


public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() {
	}

	@Override
	public void start(Stage stage) {
		UI.initialize(stage);
		UI.switchComponent(ComponentType.SERVER_CONNECT).subscribe(none -> {
			stage.show();

			new Thread(() -> {
				try {
					initialize(Configuration.getServerHost(), Configuration.getServerPort());

					SONDER_CLIENT.onEvent(ConnectionEstablishedEvent.class).subscribe(connectionEstablishedEvent -> {
						System.out.println("Connected");

						SONDER_CLIENT.onEvent(ConnectionClosedEvent.class).toMono().subscribe(connectionClosedEvent -> {
							System.out.println("Disconnected");
							Threads.runLoop(() -> {
								Thread.sleep(5000);
								System.out.println("trying to reconnect");
								try {
									SONDER_CLIENT.connect();
									authenticate();
									return false;
								}
								catch (IOException e) {
									e.printStackTrace();
									return true;
								}
							});
						});
					});

					Services.connect();

					APPLICATION_UPDATE_SERVICE.checkUpdate(Version.VERSION, Version.os).subscribe(lastVersion -> {
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
			}).start();
		});
	}

	@Override
	public void stop() throws InterruptedException, IOException {
		UI.close();
		Thread.sleep(1000);
		Services.stop();
	}

	private static void authenticate() {
		Credentials credentials = new Credentials(Configuration.getNickname(), Configuration.getPassword());
		AUTHENTICATION_SERVICE.login(credentials).subscribe(loginAnswer -> {
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
							AUTHENTICATION_SERVICE.forceLogin(credentials).subscribe(answer -> {
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
