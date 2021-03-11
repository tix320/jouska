package com.github.tix320.jouska.server.app;

import java.io.IOException;

import com.github.tix320.jouska.core.Version;

public class Application {

	public static void main(String[] args) throws IOException {
		System.out.println("Version: " + Version.CURRENT);
		AppConfig.initialize();
		AppConfig.start();
		Configuration configuration = AppConfig.INJECTOR.inject(Configuration.class);
		System.out.println("Server successfully started on port " + configuration.getPort());
	}
}
