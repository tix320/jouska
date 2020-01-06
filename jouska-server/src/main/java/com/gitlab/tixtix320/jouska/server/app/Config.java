package com.gitlab.tixtix320.jouska.server.app;

import java.nio.file.Path;
import java.util.Map;

public class Config {

	private final int port;

	private final String applicationVersion;

	private final Path sourcesPath;

	public Config(Map<String, String> properties) {
		this.port = Integer.parseInt(properties.get("port"));
		this.applicationVersion = properties.get("applicationVersion");
		this.sourcesPath = Path.of(properties.get("sourcesPath"));
	}

	public int getPort() {
		return port;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public Path getSourcesPath() {
		return sourcesPath;
	}
}
