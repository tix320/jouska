package com.github.tix320.jouska.bot.console;

import java.util.Arrays;

public final class ConsoleProgressBar {

	private static final char ICON = '▌';

	private final char[] loadingSpace;

	private double loadedPart;

	public ConsoleProgressBar() {
		loadingSpace = new char[100];
		Arrays.fill(loadingSpace, '_');
		loadedPart = 0;
	}

	public synchronized void tick(double loadedPart) {
		if (loadedPart < 0 || loadedPart > 1 || loadedPart < this.loadedPart) {
			throw new IllegalArgumentException();
		}

		this.loadedPart = loadedPart;
		int loadedPartInPercent = (int) (loadedPart * 100);
		for (int i = 0; i < loadedPartInPercent; i++) {
			loadingSpace[i] = ICON;
		}
		System.out.print("\r");
		System.out.print(ICON);
		System.out.print(loadingSpace);
		System.out.print(ICON);
		System.out.print(String.format(" %d%% Completed", loadedPartInPercent));
	}
}
