package com.github.tix320.jouska.client.ui.helper.transtion;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.util.Duration;

public final class Transitions {

	public static Transition intercept(Transition transition, Runnable runnable) {
		PauseTransition pauseTransition = new PauseTransition(Duration.seconds(0));
		pauseTransition.setOnFinished(event -> runnable.run());
		return new SequentialTransition(pauseTransition, transition);
	}

	public static Transition timeLineToTransition(Timeline timeline) {
		Transition transition = new PauseTransition(Duration.millis(1));
		transition.setOnFinished(event -> timeline.play());
		return new SequentialTransition(transition);
	}
}
