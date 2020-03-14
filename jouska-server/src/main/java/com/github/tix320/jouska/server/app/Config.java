package com.github.tix320.jouska.server.app;

import java.nio.file.Path;
import java.util.Map;

public class Config {

	private final int port;

	private final String applicationVersion;

	private final Path sourcesPath;

	private final String dbHost;

	private final int dbPort;

	public Config(Map<String, String> properties) {
		this.port = Integer.parseInt(properties.get("port"));
		this.applicationVersion = properties.get("applicationVersion");
		this.sourcesPath = Path.of(properties.getOrDefault("sourcesPath", "."));
		this.dbHost = properties.get("dbHost");
		this.dbPort = Integer.parseInt(properties.get("dbPort"));
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

	public String getDbHost() {
		return dbHost;
	}

	public int getDbPort() {
		return dbPort;
	}
}
