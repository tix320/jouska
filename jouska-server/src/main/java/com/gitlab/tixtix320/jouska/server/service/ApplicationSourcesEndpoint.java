package com.gitlab.tixtix320.jouska.server.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.gitlab.tixtix320.jouska.server.app.Application;
import com.gitlab.tixtix320.sonder.api.common.rpc.Endpoint;

@Endpoint("application")
public class ApplicationSourcesEndpoint {

	@Endpoint("latest-version")
	public String getLatestVersion() {
		return Application.config.getApplicationVersion();
	}

	@Endpoint("latest-zip")
	public byte[] getApplicationLatestSourcesZip() {
		Path installersPath = Application.config.getSourcesPath();
		if (installersPath == null) {
			throw new IllegalStateException("Sources path not specified");
		}
		else {
			try {
				return Files.readAllBytes(Path.of(installersPath + "/jouska.zip"));
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
