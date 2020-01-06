package com.gitlab.tixtix320.jouska.client.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;

import static com.gitlab.tixtix320.jouska.client.app.Services.APPLICATION_INSTALLER_SERVICE;

public class UpdateAppController implements Controller<String> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	Label messageLabel;

	@FXML
	Button updateButton;

	@Override
	public void initialize(String version) {
		messageLabel.setText("The newer version " + version + " is available.\nPlease update.");
		loadingIndicator.visibleProperty().bind(loading);
		messageLabel.disableProperty().bind(loading);
		updateButton.disableProperty().bind(loading);
	}

	public void updateApp(MouseEvent mouseEvent) {
		loading.set(true);
		APPLICATION_INSTALLER_SERVICE.getApplicationLatestSourcesZip().subscribe(bytes -> {
			try (FileOutputStream fileOutputStream = new FileOutputStream("latest.zip")) {
				fileOutputStream.write(bytes);
				Runtime.
						getRuntime().
						exec("cmd /c start \"\" unzip-latest.bat \\wait", null, new File("."));
				System.exit(0);

			}
			catch (IOException e) {
				throw new IllegalStateException(e);
			}
		});
	}
}
