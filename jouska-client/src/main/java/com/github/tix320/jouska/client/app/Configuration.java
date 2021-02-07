package com.github.tix320.jouska.client.app;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.github.tix320.jouska.core.util.ArgUtils;
import com.github.tix320.jouska.core.util.PropertiesFile;
import com.github.tix320.nimble.api.SystemProperties;

public class Configuration {

	private final PropertiesFile propertiesFile;

	private final InetSocketAddress serverAddress;

	private volatile String nickname;

	private volatile String password;

	public Configuration(Path path) {
		PropertiesFile propertiesFile = null;
		InetSocketAddress serverAddress = null;
		String nickname = null;
		String password = null;

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
			nickname = propertiesFile.getString("nickname", "");
			password = propertiesFile.getString("password", "");
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

		this.propertiesFile = propertiesFile;
		this.serverAddress = serverAddress;
		this.nickname = nickname;
		this.password = password;

	}

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}

	public synchronized void updateCredentials(String nickname, String password) {
		this.nickname = nickname;
		this.password = password;

		if (propertiesFile != null) {
			propertiesFile.set("nickname", nickname);
			propertiesFile.set("password", password);
			try {
				propertiesFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
