package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.ui.controller.ErrorController;
import com.github.tix320.jouska.client.ui.controller.ServerConnectController;
import com.github.tix320.jouska.client.ui.controller.UpdateAppController;
import com.github.tix320.jouska.core.Version;
import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

	public static void main(String[] args) {
		System.out.println("Version: " + Version.CURRENT);
		launch(args);
	}

	@Override
	public void init() {
	}

	@Override
	public void start(Stage stage) {
		AppConfig.initialize();
		UI.initialize(stage);
		stage.show();
		UI.switchComponent(ServerConnectController.class).subscribe(none -> {
			AppConfig.sonderClient.events()
					.connected()
					.toMono()
					.subscribe(connectionEstablishedEvent -> UI.switchComponent(UpdateAppController.class));

			try {
				AppConfig.connectToServer();
			} catch (IOException e) {
				e.printStackTrace();
				StringWriter out = new StringWriter();
				PrintWriter stringWriter = new PrintWriter(out);
				e.printStackTrace(stringWriter);
				UI.switchComponent(ErrorController.class, out.toString());
			}
		});
	}

	@Override
	public void stop() throws IOException {
		AppConfig.stop();
		System.exit(0);
	}
}
