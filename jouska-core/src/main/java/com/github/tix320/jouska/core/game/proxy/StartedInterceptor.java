package com.github.tix320.jouska.core.game.proxy;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.util.None;

public final class StartedInterceptor implements AnnotationInterceptor<Game, ThrowIfNotStarted> {

	@Override
	public Class<ThrowIfNotStarted> getAnnotationClass() {
		return ThrowIfNotStarted.class;
	}

	@Override
	public Object intercept(ThrowIfNotStarted annotation, InterceptionContext<Game> context) {
		if (!context.getProxy().isStarted()) {
			throw new IllegalStateException("Game does not started");
		}
		return None.SELF;
	}
}
