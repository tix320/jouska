package com.github.tix320.jouska.client.ui.transtion;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.util.Duration;

public final class TransitionInterceptor {

	public static Transition intercept(Transition transition, Runnable runnable) {
		PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0));
		pauseTransition.setOnFinished(event -> runnable.run());
		return new SequentialTransition(pauseTransition, transition);
	}
}
