package com.github.tix320.jouska.core.util;

import java.net.InetSocketAddress;

/**
 * @author : Tigran Sargsyan
 * @since : 06.02.2021
 **/
public class ArgUtils {

	public static InetSocketAddress resolveHostAndPort(String hostAndPort) {
		if (hostAndPort == null) {
			throw new IllegalArgumentException("Host not specified");
		}

		String[] parts = hostAndPort.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid host and port: " + hostAndPort);
		}

		String host = parts[0];
		int port = Integer.parseInt(parts[1]);

		return new InetSocketAddress(host, port);
	}

	public static String get
}
