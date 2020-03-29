package com.github.tix320.jouska.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class PauseableTimer {

	private final Runnable realTask;

	private final ScheduledExecutorService timer;

	private ScheduledFuture<?> lastTask;

	private long lastStartTimestamp;

	private long remainingMilliSeconds;

	public PauseableTimer(long delayMilliSeconds, Runnable task) {
		this.realTask = task;
		this.timer = Executors.newSingleThreadScheduledExecutor(Threads::daemon);
		this.remainingMilliSeconds = delayMilliSeconds;
	}

	public final synchronized void resume() {
		if (lastTask != null) {
			return;
		}
		createTaskAndSchedule(remainingMilliSeconds);
		lastStartTimestamp = System.currentTimeMillis();
	}

	public final synchronized void pause() {
		if (lastTask == null) {
			return;
		}

		lastTask.cancel(false);
		lastTask = null;
		recalculateRemainingSeconds();
	}

	public final synchronized void destroy() {
		pause();
		timer.shutdownNow();
	}

	public final synchronized long getRemainingMilliSeconds() {
		return remainingMilliSeconds;
	}

	private void recalculateRemainingSeconds() {
		long pastSecondsAfterLastResume = System.currentTimeMillis() - lastStartTimestamp;
		remainingMilliSeconds = remainingMilliSeconds - pastSecondsAfterLastResume;
	}

	private void createTaskAndSchedule(long delay) {
		lastTask = timer.schedule(realTask, delay, TimeUnit.MILLISECONDS);
	}
}
