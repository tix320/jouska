package com.github.tix320.jouska.client.ui.controller;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.jouska.client.service.origin.ApplicationUpdateOrigin;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.nimble.api.OS;
import com.github.tix320.sonder.api.common.communication.CertainReadableByteChannel;
import com.github.tix320.sonder.api.common.communication.Transfer;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class UpdateAppController implements Controller<String> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	Label messageLabel;

	@FXML
	Button updateButton;

	private final ApplicationUpdateOrigin applicationUpdateOrigin;

	public UpdateAppController(ApplicationUpdateOrigin applicationUpdateOrigin) {
		this.applicationUpdateOrigin = applicationUpdateOrigin;
	}

	@Override
	public void init(String version) {
		messageLabel.setWrapText(true);
		messageLabel.setText("The newer version " + version + " is available.\n You need to update to continue.");
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
		switch (OS.CURRENT) {
			case WINDOWS -> {
				observable = applicationUpdateOrigin.downloadClient(OS.WINDOWS);
				fileName = "jouska-windows.zip"; //TODO commands
				command = "ping 127.0.0.1 -n 6 > nul && unzip -o jouska-windows.zip && del /f jouska-windows.zip\n";
			}
			case LINUX -> {
				observable = applicationUpdateOrigin.downloadClient(OS.LINUX);
				fileName = "jouska-linux.zip";
				command = "sh sleep 5 && unzip -o jouska-linux.zip";
			}
			case MAC -> {
				observable = applicationUpdateOrigin.downloadClient(OS.MAC);
				fileName = "jouska-mac.zip";
				command = "sh sleep 5 && unzip -o jouska-mac.zip";
			}
			default -> throw new IllegalStateException(OS.CURRENT + "");
		}

		observable.subscribe(transfer -> {
			boolean ready = transfer.getHeaders().getNonNullBoolean("ready");
			if (!ready) {
				Platform.runLater(() -> {
					messageLabel.setText("Update not available now.\nPlease try later.");
					loading.setValue(false);
				});
				return;
			}

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
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
