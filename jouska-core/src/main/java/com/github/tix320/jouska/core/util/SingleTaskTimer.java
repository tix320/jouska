package com.github.tix320.jouska.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public class SingleTaskTimer {

	private long endTimeMillis;

	private final ScheduledExecutorService timer;

	private final AtomicReference<Thread> threadHolder;

	private ScheduledFuture<?> lastTask;

	public SingleTaskTimer() {
		this.threadHolder = new AtomicReference<>();
		this.timer = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = Threads.daemon(r);
			this.threadHolder.set(thread);
			return thread;
		});
	}

	public final synchronized void schedule(Runnable runnable, long delayMillis) {
		cancel();
		createTaskAndSchedule(runnable, delayMillis);
	}

	/**
	 * Return remaining milliseconds to complete timer.
	 *
	 * @return remaining millis
	 */
	public final synchronized long cancel() {
		if (lastTask != null) {
			lastTask.cancel(false);
			lastTask = null;
		}

		return Math.max(endTimeMillis - System.currentTimeMillis(), 0);
	}

	public final synchronized void destroy() {
		cancel();
		timer.shutdownNow();
		// in case when timer thread stop itself, it becomes interrupted due the timer.shutdownNow() call,
		// and continuous work of that thread is unspecified, because it may be terminated based on interrupted flag, so we are reset flag to false
		if (Thread.currentThread() == threadHolder.get()) {
			Thread.interrupted();
		}
	}

	private void createTaskAndSchedule(Runnable runnable, long delay) {
		this.endTimeMillis = System.currentTimeMillis() + delay;
		lastTask = timer.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}
}
