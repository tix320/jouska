package com.gitlab.tixtix320.jouska.server.app;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import com.gitlab.tixtix320.jouska.core.config.ConfigReader;

public class Application {

	public static Config config;

	public static void main(String[] args)
			throws InterruptedException, IOException, URISyntaxException {
		URL resource = Thread.currentThread().getContextClassLoader().getResource("config.properties");
		File configFile = new File(resource.toURI());
		ConfigReader configReader = new ConfigReader(configFile);

		config = new Config(configReader.readFromConfigFile());

		int port = config.getPort();
		Services.initialize(port);
		System.out.println("Server started on port " + port);
	}
}
