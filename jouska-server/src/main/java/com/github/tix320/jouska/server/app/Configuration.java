package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.tix320.jouska.core.util.JouskaProperties;
import com.github.tix320.deft.api.SystemProperties;

public class Configuration {

	private final InetSocketAddress serverAddress;

	private final String clientAppPAth;

	public Configuration() {
		int port;
		String portString = SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_PORT);
		port = portString == null ? 8888 : Integer.parseInt(portString);
		InetSocketAddress serverAddress;
		try {
			serverAddress = new InetSocketAddress(port);
		} catch (IllegalArgumentException e) {
			serverAddress = new InetSocketAddress("localhost", 8888);
		}

		String clientAppPath = SystemProperties.getFromEnvOrElseJava(JouskaProperties.CLIENT_APP_PATH, "client-app");

		this.serverAddress = serverAddress;
		this.clientAppPAth = clientAppPath;
	}

	public int getPort() {
		return serverAddress.getPort();
	}

	public Path getClientAppPath() {
		Path path = Path.of(clientAppPAth);
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return path;
	}
}
