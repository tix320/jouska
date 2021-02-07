package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.tix320.jouska.core.util.ArgUtils;
import com.github.tix320.jouska.core.util.JouskaProperties;
import com.github.tix320.nimble.api.SystemProperties;

public class Configuration {

	private final InetSocketAddress serverAddress;

	private final String clientAppPAth;

	public Configuration() {
		String hostPort = SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_ADDRESS);
		InetSocketAddress serverAddress;
		try {
			serverAddress = ArgUtils.resolveSocketAddress(hostPort);
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
