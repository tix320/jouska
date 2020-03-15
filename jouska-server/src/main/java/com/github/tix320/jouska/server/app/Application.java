package com.github.tix320.jouska.server.app;

import java.io.IOException;
import java.io.InputStream;

public class Application {

	public static void main(String[] args)
			throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");

		int port = Configuration.getPort();
		DataSource.init();
		Services.initialize(port);
		System.out.println("Server started on port " + port);
	}
}
