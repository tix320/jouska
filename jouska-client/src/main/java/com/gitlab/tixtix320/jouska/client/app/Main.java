package com.gitlab.tixtix320.jouska.client.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.gitlab.tixtix320.jouska.core.config.ConfigReader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import static com.gitlab.tixtix320.jouska.client.app.Services.APPLICATION_INSTALLER_SERVICE;


public class Main extends Application {

	private static Config config;

	public static void main(String[] args)
			throws InterruptedException, IOException {
		launch(args);
	}

	@Override
	public void init()
			throws Exception {
	}

	@Override
	public void start(Stage stage)
			throws Exception {
		Jouska.initialize(stage);
		try {
			ConfigReader configReader = new ConfigReader(new File("config.properties"));

			config = new Config(configReader.readFromConfigFile());
			Services.initialize(config.getServerHost(), config.getServerPort());
			APPLICATION_INSTALLER_SERVICE.getLatestVersion().subscribe(latestVersion -> {
				if (config.getApplicationVersion().equals(latestVersion)) {
					Jouska.switchScene("menu");
				}
				else {
					Jouska.switchScene("update-app", latestVersion);
				}
				Platform.runLater(stage::show);
			});
		}
		catch (Exception e) {
			e.printStackTrace();
			stage.show();
			StringWriter out = new StringWriter();
			PrintWriter stringWriter = new PrintWriter(out);
			e.printStackTrace(stringWriter);
			Jouska.switchScene("error", out.toString());
		}
	}

	@Override
	public void stop()
			throws Exception {
		Services.stop();
	}
}
