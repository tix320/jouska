package com.github.tix320.jouska.client.app;

public class Version {

	public static final String VERSION = "@version@";

	public static final OS os = OS.valueOf("@os@");

	public enum OS {
		WINDOWS,
		LINUX,
		MAC
	}
}
