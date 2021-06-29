package com.github.tix320.jouska.bot.config;

import com.github.tix320.deft.api.SystemProperties;
import com.github.tix320.jouska.core.util.JouskaProperties;
import com.github.tix320.jouska.core.util.PropertiesFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

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

		String host = null;
		Integer port = null;
		if (propertiesFile != null) {
			host = propertiesFile.getString(JouskaProperties.SERVER_HOST);
			port = propertiesFile.getInt(JouskaProperties.SERVER_PORT);
		}

		if (host == null) {
			host = SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_HOST,
														 JouskaProperties.DEFAULT_SERVER_HOST);
		}

		if (port == null) {
			port = Integer.parseInt(SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_PORT, String.valueOf(
					JouskaProperties.DEFAULT_SERVER_PORT)));
		}

		try {
			serverAddress = new InetSocketAddress(host, port);
		} catch (IllegalArgumentException ignored) {
			System.err.printf("Host and port are invalid: %s:%s . Using defaults: %s:%s%n", host, port,
							  JouskaProperties.DEFAULT_SERVER_HOST, JouskaProperties.DEFAULT_SERVER_PORT);
			serverAddress = new InetSocketAddress(JouskaProperties.DEFAULT_SERVER_HOST,
												  JouskaProperties.DEFAULT_SERVER_PORT);
		}

		System.out.printf("Server host-port: %s%n", serverAddress);

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
