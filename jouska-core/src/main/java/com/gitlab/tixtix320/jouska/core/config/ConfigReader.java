package com.gitlab.tixtix320.jouska.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public final class ConfigReader {

	private final InputStream inputStream;

	public ConfigReader(File file)
			throws IOException {
		this.inputStream = new FileInputStream(file);
	}

	public ConfigReader(InputStream inputStream)
			throws IOException {
		this.inputStream = inputStream;
	}

	public Map<String, String> readFromConfigFile()
			throws IOException {
		Properties props = new Properties();
		props.load(inputStream);

		return (Map) props;
	}
}
