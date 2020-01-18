package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import com.github.tix320.jouska.core.config.ConfigReader;

public class Application {

	public static Config config;

	public static void main(String[] args)
			throws InterruptedException, IOException, URISyntaxException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		ConfigReader configReader = new ConfigReader(stream);

		config = new Config(configReader.readFromConfigFile());

		int port = config.getPort();
		Services.initialize(port);
		System.out.println("Server started on port " + port);
	}
}
