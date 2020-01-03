package com.gitlab.tixtix320.jouska.server.app;

public class Main {

	public static void main(String[] args) {
		int port = 8888;
		Services.initialize(port);
		System.out.println("Server started on port " + port);
	}
}
