package com.github.tix320.jouska.client.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.core.config.ConfigReader;
import javafx.application.Application;
import javafx.stage.Stage;

import static com.github.tix320.jouska.client.app.Services.APPLICATION_INSTALLER_SERVICE;


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
		JouskaUI.initialize(stage);
		JouskaUI.switchScene("server-connect").subscribe(none -> {
			stage.show();

			new Thread(() -> {
				try {
					ConfigReader configReader = new ConfigReader(new File("config.properties"));

					config = new Config(configReader.readFromConfigFile());
					Services.initialize(config.getServerHost(), config.getServerPort());
					APPLICATION_INSTALLER_SERVICE.checkUpdate(Version.VERSION, Version.os.name())
							.subscribe(lastVersion -> {
								if (!lastVersion.equals("")) { // update
									JouskaUI.switchScene("update-app", lastVersion);
								}
								else {
									JouskaUI.switchScene("menu");
								}
							});
				}
				catch (Exception e) {
					e.printStackTrace();
					StringWriter out = new StringWriter();
					PrintWriter stringWriter = new PrintWriter(out);
					e.printStackTrace(stringWriter);
					JouskaUI.switchScene("error", out.toString());
				}
			}).start();
		});
	}

	@Override
	public void stop()
			throws Exception {
		JouskaUI.close();
		Services.stop();
	}
}
