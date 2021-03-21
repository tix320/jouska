package com.github.tix320.jouska.client.ui.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import com.github.tix320.deft.api.OS;
import com.github.tix320.jouska.client.app.AppProperties;
import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.service.origin.ApplicationUpdateOrigin;
import com.github.tix320.jouska.core.Version;
import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.update.UpdateNotReadyException;
import com.github.tix320.jouska.core.update.UpdateRunner;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.skimp.api.object.None;
import com.github.tix320.sonder.api.common.communication.Transfer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class UpdateAppController implements Controller<None> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(true);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	Label messageLabel;

	@FXML
	Button updateButton;

	private final Configuration configuration;

	private final ApplicationUpdateOrigin applicationUpdateOrigin;

	private volatile Version version;

	public UpdateAppController(Configuration configuration, ApplicationUpdateOrigin applicationUpdateOrigin) {
		this.configuration = configuration;
		this.applicationUpdateOrigin = applicationUpdateOrigin;
	}

	@Override
	public void init(None none) {
		messageLabel.setWrapText(true);
		loadingIndicator.visibleProperty().bind(loading);
		messageLabel.disableProperty().bind(loading);
		updateButton.disableProperty().bind(loading);

		messageLabel.setText("Checking for newer version...");

		applicationUpdateOrigin.getVersion().subscribe(lastVersion -> {
			this.version = lastVersion;
			int compareResult = lastVersion.compareTo(Version.CURRENT);
			if (compareResult < 0) {
				final String error = "Illegal state: Client version higher than server version. Server - %s, Client - %s."
						.formatted(lastVersion, Version.CURRENT);
				UI.switchComponent(ErrorController.class, error);
			} else if (compareResult > 0) { // update
				Platform.runLater(() -> {
					loading.set(false);
					messageLabel.setText(
							"The newer version " + lastVersion + " is available.\n You need to update to continue.");
				});
			} else {
				Credentials credentials = new Credentials(configuration.getNickname(), configuration.getPassword());
				UI.switchComponent(LoginController.class, credentials).subscribe(LoginController::login);
			}
		});
	}

	@Override
	public void destroy() {

	}

	public void updateApp() {
		loading.set(true);

		CompletableFuture.runAsync(() -> {
			final Transfer transfer;
			try {
				transfer = applicationUpdateOrigin.downloadClient(OS.CURRENT).get(Duration.ofSeconds(30));
			} catch (TimeoutException e) {
				messageLabel.setText("Timeout");
				loading.set(false);
				throw e;
			}

			UpdateRunner updateRunner = new UpdateRunner(AppProperties.APP_HOME_DIRECTORY, version,
					progress -> Platform.runLater(() -> loadingIndicator.setProgress(progress)));

			try {
				updateRunner.update(transfer);
			} catch (UpdateNotReadyException e) {
				Platform.runLater(() -> {
					messageLabel.setText("Update not available now.\nPlease try later.");
					loading.setValue(false);
				});
				try {
					transfer.contentChannel().close();
				} catch (IOException ignored) {

				}
			} catch (Throwable e) {
				e.printStackTrace();
				Platform.runLater(() -> {
					messageLabel.setText("""
							Update failed.
							Error code: 1015.
							%s""".formatted(e.getMessage()));
					loading.setValue(false);
				});
			}
		});
	}
}
