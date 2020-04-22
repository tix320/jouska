package com.github.tix320.jouska.client.ui.helper;

import javafx.application.Platform;

/**
 * @author Tigran Sargsyan on 14-Apr-20.
 */
public class FXHelper {

	public static void checkFxThread() {
		// Throw exception if not on FX user thread
		if (!Platform.isFxApplicationThread()) {
			throw new IllegalStateException(
					"Not on FX application thread; currentThread = " + Thread.currentThread().getName());
		}
	}
}
