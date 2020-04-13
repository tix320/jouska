package com.github.tix320.jouska.client.ui.helper.component;

import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public final class TimerLabel extends LocalTimeLabel {

	private final AtomicReference<Timeline> timeline = new AtomicReference<>(null);

	public void run() {
		run(1);
	}

	public void run(double speedCoefficient) {
		stop();

		Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1 / speedCoefficient), ae -> {
			LocalTime time = getTime();
			if (time.equals(LocalTime.MIN)) {
				stop();
			}
			else {
				setTime(time.minusSeconds(1));
			}
		}));

		this.timeline.set(timeline);

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.play();
	}

	public void stop() {
		this.timeline.updateAndGet(timeline -> {
			if (timeline != null) {
				timeline.stop();
			}
			return null;
		});
	}
}
