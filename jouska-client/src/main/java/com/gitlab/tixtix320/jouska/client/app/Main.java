package com.gitlab.tixtix320.jouska.client.app;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

	public static void main(String[] args)
			throws InterruptedException {
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
			Services.initialize("3.230.34.96", 8888);

		}
		catch (Exception e) {
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
