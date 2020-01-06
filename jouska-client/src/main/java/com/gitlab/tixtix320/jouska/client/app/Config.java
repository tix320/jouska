package com.gitlab.tixtix320.jouska.client.app;

import java.util.Map;

public final class Config {

	private final String serverHost;

	private final int serverPort;

	private final String applicationVersion;

	public Config(Map<String, String> properties) {
		this.serverHost = properties.get("serverHost");
		this.serverPort = Integer.parseInt(properties.get("serverPort"));
		this.applicationVersion = properties.get("applicationVersion");
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}
}
