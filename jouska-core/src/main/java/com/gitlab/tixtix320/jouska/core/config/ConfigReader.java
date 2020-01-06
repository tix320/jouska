package com.gitlab.tixtix320.jouska.core.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public final class ConfigReader {

	private final File configFile;

	public ConfigReader(File configFile)
			throws IOException {
		this.configFile = configFile;
	}

	public Map<String, String> readFromConfigFile()
			throws IOException {
		Properties props = new Properties();
		props.load(new FileReader(configFile));

		return (Map) props;
	}
}
