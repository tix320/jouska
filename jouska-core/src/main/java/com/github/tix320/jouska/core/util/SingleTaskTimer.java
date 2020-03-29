package com.github.tix320.jouska.core.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class SingleTaskTimer {

	private final Timer timer;

	private volatile TimerTask lastTimerTask;

	public SingleTaskTimer() {
		this.timer = new Timer(true);
	}

	public final synchronized void schedule(Runnable runnable, long delayMillis) {
		cancel();

		createTaskAndSchedule(runnable, delayMillis);
	}

	public final synchronized void cancel() {
		if (lastTimerTask != null) {
			lastTimerTask.cancel();
			lastTimerTask = null;
		}
	}

	public final void destroy() {
		cancel();
		timer.cancel();
	}

	private void createTaskAndSchedule(Runnable runnable, long delay) {
		lastTimerTask = new TimerTask() {
			@Override
			public void run() {
				runnable.run();
			}
		};
		timer.schedule(lastTimerTask, delay);
	}
}
