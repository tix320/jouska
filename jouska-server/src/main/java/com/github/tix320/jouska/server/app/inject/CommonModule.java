package com.github.tix320.jouska.server.app.inject;

import com.github.tix320.jouska.server.app.Configuration;
import com.github.tix320.ravel.api.scope.Singleton;

/**
 * @author : Tigran Sargsyan
 * @since : 05.02.2021
 **/
public class CommonModule {

	@Singleton
	public Configuration configuration() {
		return new Configuration();
	}
}