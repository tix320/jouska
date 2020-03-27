package com.github.tix320.jouska.client.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {

	private static final Map<String, String> properties = new HashMap<>();

	static {
		try (InputStream inputStream = new FileInputStream("config.properties")) {
			Properties properties = new Properties();
			properties.load(inputStream);
			Configuration.properties.putAll((Map) properties);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getServerHost() {
		return properties.getOrDefault("serverHost", "localhost");
	}

	public static int getServerPort() {
		return Integer.parseInt(properties.getOrDefault("serverPort", "8888"));
	}

	public static String getNickname() {
		return properties.getOrDefault("nickname", "");
	}

	public static String getPassword() {
		return properties.getOrDefault("password", "");
	}

	public static void updateCredentials(String username, String password) {
		properties.put("nickname", username);
		properties.put("password", password);
		flush();
	}

	private static void flush() {
		Properties properties = new Properties();
		properties.putAll(Configuration.properties);
		try (FileOutputStream outputStream = new FileOutputStream("config.properties")) {
			properties.store(outputStream, null);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}