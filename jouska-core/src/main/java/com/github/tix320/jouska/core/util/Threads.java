package com.github.tix320.jouska.core.util;

import com.github.tix320.jouska.core.util.LoopThread.LoopRunner;

/**
 * @author Tigran Sargsyan on 27-Mar-20.
 */
public final class Threads {

	public static LoopThread runLoop(LoopRunner runnable) {
		LoopThread loopThread = new LoopThread(runnable);
		loopThread.start();
		return loopThread;
	}

	public static Thread daemon(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}
}
