package com.github.tix320.jouska.server.app;

import java.io.IOException;

public class Application {

	public static void main(String[] args) throws IOException {
		AppConfig.initialize();
		AppConfig.start();
		Configuration configuration = AppConfig.INJECTOR.inject(Configuration.class);
		System.out.println("Server successfully started on port " + configuration);
	}
}
