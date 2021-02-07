package com.github.tix320.jouska.bot.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.github.tix320.jouska.core.util.ArgUtils;
import com.github.tix320.jouska.core.util.JouskaProperties;
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

		String hostPort;
		if (propertiesFile != null) {
			hostPort = propertiesFile.getString(JouskaProperties.SERVER_ADDRESS);
		} else {
			hostPort = SystemProperties.getFromEnvOrElseJava(JouskaProperties.SERVER_ADDRESS);
		}
		try {
			serverAddress = ArgUtils.resolveSocketAddress(hostPort);
		} catch (IllegalArgumentException ignored) {

		}

		if (serverAddress == null) {
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
			propertiesFile.putIfAbsent(JouskaProperties.SERVER_ADDRESS,
					serverAddress.getHostName() + ":" + serverAddress.getPort());
			try {
				propertiesFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
