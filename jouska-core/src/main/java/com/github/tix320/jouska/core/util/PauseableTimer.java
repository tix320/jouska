package com.github.tix320.jouska.core.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public class PauseableTimer {

	private final Runnable realTask;

	private final Timer timer;

	private TimerTask task;

	private long lastStartTimestamp;

	private long remainingMilliSeconds;

	public PauseableTimer(long delayMilliSeconds, Runnable task) {
		this.realTask = task;
		this.timer = new Timer(true);
		this.remainingMilliSeconds = delayMilliSeconds;
	}

	public final synchronized void resume() {
		if (task != null) {
			return;
		}
		createTaskAndSchedule(remainingMilliSeconds);
		lastStartTimestamp = System.currentTimeMillis();
	}

	public final synchronized void pause() {
		if (task == null) {
			return;
		}

		task.cancel();
		task = null;
		recalculateRemainingSeconds();
	}

	public final void destroy() {
		pause();
		timer.cancel();
	}

	public final synchronized long getRemainingMilliSeconds() {
		return remainingMilliSeconds;
	}

	private void recalculateRemainingSeconds() {
		long pastSecondsAfterLastResume = System.currentTimeMillis() - lastStartTimestamp;
		remainingMilliSeconds = remainingMilliSeconds - pastSecondsAfterLastResume;
	}

	private void createTaskAndSchedule(long delay) {
		task = new TimerTask() {
			@Override
			public void run() {
				realTask.run();
			}
		};
		timer.schedule(task, delay);
	}
}
