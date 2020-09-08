package com.github.tix320.jouska.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.skimp.api.thread.Threads;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class PauseableTimer {

	private final Runnable realTask;

	private final ScheduledExecutorService timer;

	private final AtomicReference<Thread> threadHolder;

	private ScheduledFuture<?> task;

	private long lastStartTimestamp;

	private long remainingMilliSeconds;

	public PauseableTimer(long delayMilliSeconds, Runnable task) {
		this.realTask = task;
		this.threadHolder = new AtomicReference<>();
		this.timer = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = Threads.daemon(r);
			this.threadHolder.set(thread);
			return thread;
		});
		this.remainingMilliSeconds = delayMilliSeconds;
	}

	public final synchronized void resume() {
		if (task != null) {
			return;
		}
		createTaskAndSchedule(remainingMilliSeconds);
		lastStartTimestamp = System.currentTimeMillis();
	}

	/**
	 * Return remaining milliseconds to complete timer.
	 *
	 * @return remaining millis
	 */
	public final synchronized long pause() {
		if (task == null) {
			return remainingMilliSeconds;
		}

		task.cancel(false);
		task = null;
		recalculateRemainingSeconds();
		return remainingMilliSeconds;
	}

	public final synchronized void destroy() {
		pause();
		timer.shutdownNow();
		// in case when timer thread stop itself, it becomes interrupted due the timer.shutdownNow() call,
		// and continuous work of thread is unspecified, because it may be terminated based on interrupted flag, so we are reset flag to false
		if (Thread.currentThread() == threadHolder.get()) {
			Thread.interrupted();
		}
	}

	public final synchronized boolean isDestroyed() {
		return timer.isShutdown();
	}

	public final synchronized long getRemainingMilliSeconds() {
		return remainingMilliSeconds;
	}

	private void recalculateRemainingSeconds() {
		long pastSecondsAfterLastResume = System.currentTimeMillis() - lastStartTimestamp;
		remainingMilliSeconds = remainingMilliSeconds - pastSecondsAfterLastResume;
	}

	private void createTaskAndSchedule(long delay) {
		task = timer.schedule(realTask, delay, TimeUnit.MILLISECONDS);
	}
}
