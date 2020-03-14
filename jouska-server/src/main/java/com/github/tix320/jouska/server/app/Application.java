package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.io.InputStream;

import com.github.tix320.jouska.core.config.ConfigReader;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.entity.PlayerEntity;

public class Application {

	public static Config config;

	public static void main(String[] args)
			throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");
		ConfigReader configReader = new ConfigReader(stream);

		config = new Config(configReader.readFromConfigFile());

		int port = config.getPort();
		Services.initialize(port);
		DataSource.init(config.getDbHost(), config.getDbPort());
		System.out.println("Server started on port " + port);
	}
}
