package com.github.tix320.jouska.client.ui.controller;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.jouska.client.app.Version;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.sonder.api.common.communication.CertainReadableByteChannel;
import com.github.tix320.sonder.api.common.communication.Transfer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import static com.github.tix320.jouska.client.app.Services.APPLICATION_INSTALLER_SERVICE;

public class UpdateAppController implements Controller<String> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	Label messageLabel;

	@FXML
	Button updateButton;

	@Override
	public void init(String version) {
		messageLabel.setText("The newer version " + version + " is available.\nPlease update.");
		loadingIndicator.visibleProperty().bind(loading);
		messageLabel.disableProperty().bind(loading);
		updateButton.disableProperty().bind(loading);
	}

	@Override
	public void destroy() {

	}

	public void updateApp() {
		loading.set(true);
		Observable<Transfer> observable;
		String fileName;
		String command;
		switch (Version.os) {
			case WINDOWS:
				observable = APPLICATION_INSTALLER_SERVICE.downloadWindowsLatest();
				fileName = "jouska-windows.zip";
				command = "cmd /c start \"\" update.bat";
				break;
			case LINUX:
				observable = APPLICATION_INSTALLER_SERVICE.downloadLinuxLatest();
				fileName = "jouska-linux.zip";
				command = "sh update-linux.sh";
				break;
			case MAC:
				observable = APPLICATION_INSTALLER_SERVICE.downloadMacLatest();
				fileName = "jouska-mac.zip";
				command = "sh update-mac.sh";
				break;
			default:
				throw new IllegalStateException(Version.os + "");
		}

		observable.subscribe(transfer -> {
			CertainReadableByteChannel channel = transfer.channel();
			long zipLength = channel.getContentLength();
			int consumedBytes = 0;

			try (FileChannel fileChannel = FileChannel.open(Path.of(fileName), StandardOpenOption.CREATE,
					StandardOpenOption.WRITE)) {
				ByteBuffer buffer = ByteBuffer.allocate(1024 * 64);
				int read;
				while ((read = channel.read(buffer)) != -1) {
					buffer.flip();
					fileChannel.write(buffer);
					buffer.clear();
					consumedBytes += read;
					final double progress = (double) consumedBytes / zipLength;
					Platform.runLater(() -> loadingIndicator.setProgress(progress));
				}

				Runtime.
						getRuntime().
						exec(command, null, new File("."));
				System.exit(0);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
