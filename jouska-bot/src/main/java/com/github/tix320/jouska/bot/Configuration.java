package com.github.tix320.jouska.bot;

import java.io.FileInputStream;
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
}
