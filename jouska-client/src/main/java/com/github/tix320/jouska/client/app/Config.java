package com.github.tix320.jouska.client.app;

import java.util.Map;

public final class Config {

	private final String serverHost;

	private final int serverPort;

	public Config(Map<String, String> properties) {
		this.serverHost = properties.get("serverHost");
		this.serverPort = Integer.parseInt(properties.get("serverPort"));
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}
}
