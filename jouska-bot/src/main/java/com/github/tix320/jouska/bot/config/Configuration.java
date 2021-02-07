package com.github.tix320.jouska.bot.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.github.tix320.jouska.core.util.ArgUtils;
import com.github.tix320.jouska.core.util.PropertiesFile;
import com.github.tix320.nimble.api.SystemProperties;

public class Configuration {

	private final InetSocketAddress serverAddress;

	public Configuration(Path path) {
		PropertiesFile propertiesFile = null;
		InetSocketAddress serverAddress = null;

		try {
			propertiesFile = PropertiesFile.of(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (propertiesFile != null) {
			String hostPort = propertiesFile.getString("server.host-port");
			try {
				serverAddress = ArgUtils.resolveHostAndPort(hostPort);
			} catch (IllegalArgumentException ignored) {

			}
		} else {
			String hostPort = SystemProperties.getFromEnvOrElseJava("jouska.server.host-port");
			try {
				serverAddress = ArgUtils.resolveHostAndPort(hostPort);
			} catch (IllegalArgumentException ignored) {

			}
		}

		if (serverAddress == null) {
			serverAddress = new InetSocketAddress("localhost", 8888);
		}

		this.serverAddress = serverAddress;

	}

	public String getServerHost() {
		return serverAddress.getHostName();
	}

	public int getServerPort() {
		return serverAddress.getPort();
	}
}
