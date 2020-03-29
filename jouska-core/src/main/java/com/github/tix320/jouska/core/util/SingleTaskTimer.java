package com.github.tix320.jouska.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class SingleTaskTimer {

	private final ScheduledExecutorService timer;

	private ScheduledFuture<?> lastTask;

	public SingleTaskTimer() {
		this.timer = Executors.newSingleThreadScheduledExecutor(Threads::daemon);
	}

	public final synchronized void schedule(Runnable runnable, long delayMillis) {
		cancel();

		createTaskAndSchedule(runnable, delayMillis);
	}

	public final synchronized void cancel() {
		if (lastTask != null) {
			lastTask.cancel(false);
			lastTask = null;
		}
	}

	public final synchronized void destroy() {
		cancel();
		timer.shutdownNow();
	}

	private void createTaskAndSchedule(Runnable runnable, long delay) {
		lastTask = timer.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}
}
