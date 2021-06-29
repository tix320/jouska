package com.github.tix320.jouska.client.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import com.github.tix320.deft.api.SystemProperties;
import com.github.tix320.jouska.core.util.JouskaProperties;
import com.github.tix320.jouska.core.util.PropertiesFile;

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

		String host = null;
		Integer port = null;
		if (propertiesFile != null) {
			host = propertiesFile.getString(JouskaProperties.SERVER_HOST);
			port = propertiesFile.getInt(JouskaProperties.SERVER_PORT);
			nickname = propertiesFile.getString(JouskaProperties.NICKNAME, "");
			password = propertiesFile.getString(JouskaProperties.PASSWORD, "");
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

		this.propertiesFile = propertiesFile;
		this.serverAddress = serverAddress;
		this.nickname = nickname;
		this.password = password;

		fillPropertiesFile(propertiesFile);
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
			propertiesFile.put(JouskaProperties.NICKNAME, nickname);
			propertiesFile.put(JouskaProperties.PASSWORD, password);
			try {
				propertiesFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
