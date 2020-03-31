package com.github.tix320.jouska.bot;

import java.util.Arrays;

public final class ProgressBar {

    private static final char ICON = '▌';
    
    private final char[] loadingSpace;

    private long loadedPart;

    public ProgressBar() {
        loadingSpace = new char[100];
        Arrays.fill(loadingSpace, '_');
        loadedPart = 0;
    }

    public synchronized void tick(long loadedPart) {
        if (loadedPart < this.loadedPart) {
            return;
        }
        this.loadedPart = loadedPart;
        for (int i = 0; i < loadedPart; i++) {
            loadingSpace[i] = ICON;
        }
        System.out.print("\r");
        System.out.print(ICON);
        System.out.print(loadingSpace);
        System.out.print(ICON);
        System.out.print(" " + loadedPart + "% Completed");
    }
}
