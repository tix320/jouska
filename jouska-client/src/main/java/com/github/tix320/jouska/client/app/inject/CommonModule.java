package com.github.tix320.jouska.client.app.inject;

import java.nio.file.Path;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.nimble.api.SystemProperties;
import com.github.tix320.ravel.api.scope.Singleton;

/**
 * @author : Tigran Sargsyan
 * @since : 05.02.2021
 **/
public class CommonModule {

	@Singleton
	public Configuration configuration() {
		return new Configuration(Path.of(SystemProperties.getUserDirectory(), "jouska", "config.properties"));
	}
}
