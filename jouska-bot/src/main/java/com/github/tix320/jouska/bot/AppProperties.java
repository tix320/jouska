package com.github.tix320.jouska.bot;

import java.nio.file.Path;

import com.github.tix320.deft.api.SystemProperties;

public class AppProperties {

	public static final Path APP_HOME_DIRECTORY = SystemProperties.USER_DIRECTORY.resolve(".jouska-bot");

	public static final Path APP_CONFIG_FILE = APP_HOME_DIRECTORY.resolve("config.properties");
}
