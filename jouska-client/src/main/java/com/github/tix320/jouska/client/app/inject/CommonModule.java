package com.github.tix320.jouska.client.app.inject;

import com.github.tix320.jouska.client.app.AppProperties;
import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.ravel.api.scope.Singleton;

/**
 * @author : Tigran Sargsyan
 * @since : 05.02.2021
 **/
public class CommonModule {

	@Singleton
	public Configuration configuration() {
		return new Configuration(AppProperties.APP_CONFIG_FILE);
	}
}
