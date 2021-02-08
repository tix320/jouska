package com.github.tix320.jouska.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : Tigran Sargsyan
 * @since : 05.02.2021
 **/
public class PropertiesFile {

	private final File file;

	private final Map<String, String> properties;

	private PropertiesFile(Path path) throws IOException {
		createFileIfNotValid(path);
		this.file = path.toFile();
		Properties properties = new Properties();
		try (FileInputStream stream = new FileInputStream(file)) {
			properties.load(stream);
		}

		@SuppressWarnings("all")
		Map<String, String> castedMap = new ConcurrentHashMap<>((Map) properties);
		this.properties = castedMap;
	}

	public static PropertiesFile of(Path path) throws IOException {
		return new PropertiesFile(path);
	}

	public String getString(String key) {
		return properties.get(key);
	}

	public String getString(String key, String defaultValue) {
		String value = properties.get(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public int getInt(String key) {
		return Integer.parseInt(getString(key));
	}

	public int getInt(String key, int defaultValue) {
		String value = getString(key, defaultValue + "");
		return Integer.parseInt(value);
	}

	public void put(String key, String value) {
		properties.put(key, value);
	}

	public void putIfAbsent(String key, String value) {
		properties.putIfAbsent(key, value);
	}

	public synchronized void flush() throws IOException {
		Properties properties = new Properties();
		properties.putAll(this.properties);
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			properties.store(outputStream, null);
		}
	}

	private static void createFileIfNotValid(Path path) throws IOException {
		boolean needCreate;
		if (Files.exists(path)) {
			if (Files.isRegularFile(path)) {
				needCreate = false;
			} else {
				Files.delete(path);
				needCreate = true;
			}
		} else {
			needCreate = true;
		}

		if (needCreate) {
			Path parentDirectory = path.getParent();
			Files.createDirectories(parentDirectory);
			Files.createFile(path);
		}
	}
}
