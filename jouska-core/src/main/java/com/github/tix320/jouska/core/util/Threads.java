package com.github.tix320.jouska.core.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public final class Threads {

	public static AtomicBoolean runLoop(BooleanSupplier runnable) {
		AtomicBoolean running = new AtomicBoolean(true);
		Thread thread = new Thread(() -> {
			while (running.get()) {
				try {
					boolean needContinue = runnable.getAsBoolean();
					if (!needContinue) {
						break;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.setDaemon(true);
		thread.start();
		return running;
	}

	public static Thread daemon(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}
}
