package com.github.tix320.jouska.core.util;

import java.net.InetSocketAddress;

/**
 * @author : Tigran Sargsyan
 * @since : 06.02.2021
 **/
public class ArgUtils {

	public static InetSocketAddress resolveSocketAddress(String address) {
		if (address == null) {
			throw new IllegalArgumentException("Address not specified");
		}

		String[] parts = address.split(":");
		if (parts.length != 2) {
			throw new IllegalArgumentException("Invalid address: " + address);
		}

		String host = parts[0];
		int port = Integer.parseInt(parts[1]);

		return new InetSocketAddress(host, port);
	}
}
