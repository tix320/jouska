package com.github.tix320.jouska.bot.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.github.tix320.jouska.core.util.JouskaProperties;
import com.github.tix320.jouska.core.util.PropertiesFile;
import com.github.tix320.deft.api.SystemProperties;

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

		String host;
		Integer port;
		if (propertiesFile != null) {
			host = propertiesFile.getString(JouskaProperties.SERVER_HOST);
			port = propertiesFile.getInt(JouskaProperties.SERVER_PORT);
		} else {
			host = SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_HOST);
			port = Integer.parseInt(SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_PORT));
		}
		try {
			if (port == null) {
				port = 8888;
			}
			serverAddress = new InetSocketAddress(host, port);
		} catch (IllegalArgumentException ignored) {
			serverAddress = new InetSocketAddress("localhost", 8888);
		}

		this.serverAddress = serverAddress;

		fillPropertiesFile(propertiesFile);
	}

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	private void fillPropertiesFile(PropertiesFile propertiesFile) {
		if (propertiesFile != null) {
			propertiesFile.putIfAbsent(JouskaProperties.SERVER_HOST, serverAddress.getHostName());
			propertiesFile.putIfAbsent(JouskaProperties.SERVER_PORT, String.valueOf(serverAddress.getPort()));
			try {
				propertiesFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
