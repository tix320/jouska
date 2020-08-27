package com.github.tix320.jouska.server.app;

public class Application {

	public static void main(String[] args) {
		int port = Configuration.getPort();
		AppConfig.initialize(port);
		System.out.println("Server successfully started on port " + port);
	}
}
