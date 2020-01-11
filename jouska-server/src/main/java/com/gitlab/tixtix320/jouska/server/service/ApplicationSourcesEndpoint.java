package com.gitlab.tixtix320.jouska.server.service;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.gitlab.tixtix320.jouska.server.app.Application;
import com.gitlab.tixtix320.sonder.api.common.communication.ChannelTransfer;
import com.gitlab.tixtix320.sonder.api.common.communication.Headers;
import com.gitlab.tixtix320.sonder.api.common.communication.Transfer;
import com.gitlab.tixtix320.sonder.api.common.rpc.Endpoint;

@Endpoint("application")
public class ApplicationSourcesEndpoint {

	@Endpoint("latest-version")
	public String getLatestVersion() {
		return Application.config.getApplicationVersion();
	}

	@Endpoint("latest-zip")
	public Transfer getApplicationLatestSourcesZip() {
		Path installersPath = Application.config.getSourcesPath();
		if (installersPath == null) {
			throw new IllegalStateException("Sources path not specified");
		}
		else {
			try {
				Path file = Path.of(installersPath + "/jouska.zip");
				long length = Files.size(file);
				return new ChannelTransfer(Headers.EMPTY, FileChannel.open(file, StandardOpenOption.READ),
						(int) length);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
