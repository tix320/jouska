package com.github.tix320.jouska.server.app;

import java.io.IOException;

public class Application {

	public static void main(String[] args)
			throws IOException {
		int port = Configuration.getPort();
		Services.initialize(port);
		System.out.println("Server successfully started on port " + port);
	}
}
