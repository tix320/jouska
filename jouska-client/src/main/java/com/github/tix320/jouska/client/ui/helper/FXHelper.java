package com.github.tix320.jouska.client.ui.helper;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class FXHelper {

	public static void checkFxThread() {
		// Throw exception if not on FX user thread
		if (!Platform.isFxApplicationThread()) {
			throw new IllegalStateException(
					"Not on FX application thread; currentThread = " + Thread.currentThread().getName());
		}
	}

	public static void loadFxmlForController(String fxmlPath, Object controller) {
		FXMLLoader fxmlLoader = new FXMLLoader(controller.getClass().getResource(fxmlPath));
		fxmlLoader.setRoot(controller);
		fxmlLoader.setController(controller);
		try {
			fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
