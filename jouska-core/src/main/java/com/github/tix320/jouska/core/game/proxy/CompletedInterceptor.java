package com.github.tix320.jouska.core.game.proxy;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.util.None;

public class CompletedInterceptor implements AnnotationInterceptor<Game, ThrowIfCompleted> {

	@Override
	public Class<ThrowIfCompleted> getAnnotationClass() {
		return ThrowIfCompleted.class;
	}

	@Override
	public Object intercept(ThrowIfCompleted annotation, InterceptionContext<Game> context) {
		if (context.getProxy().isCompleted()) {
			throw new IllegalStateException("Game already completed");
		}
		return None.SELF;
	}
}
