package com.github.tix320.jouska.server.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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

	public static int getPort() {
		return Integer.parseInt(properties.getOrDefault("port", "8888"));
	}

	public static String getApplicationVersion() {
		return properties.get("applicationVersion");
	}

	public static Path getClientAppPath() {
		return Path.of(properties.getOrDefault("clientAppPath", "client-app"));
	}
}
