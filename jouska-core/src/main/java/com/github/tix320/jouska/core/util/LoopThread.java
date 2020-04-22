package com.github.tix320.jouska.core.util;


/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class LoopThread {

	private final Thread thread;

	public LoopThread(LoopRunner runner) {
		thread = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					boolean needContinue = runner.run();
					if (!needContinue) {
						break;
					}
				}
				catch (InterruptedException e) {
					break;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		thread.setDaemon(true);
	}

	public void start() {
		thread.start();
	}

	public void stop() {
		thread.interrupt();
	}

	public interface LoopRunner {

		boolean run() throws InterruptedException;
	}
}
