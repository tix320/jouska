package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.bot.process.BotProcess;
import com.github.tix320.sonder.api.client.SonderClient;

/**
 * @author Tigran Sargsyan on 09-May-20.
 */
public class Context {

	private static SonderClient sonderClient;

	private static BotProcess botProcess;

	public static BotProcess getBotProcess() {
		if (botProcess == null) {
			throw new IllegalStateException();
		}
		return botProcess;
	}

	public static void setBotProcess(BotProcess botProcess) {
		Context.botProcess = botProcess;
	}

	public static SonderClient getSonderClient() {
		if (sonderClient == null) {
			throw new IllegalStateException();
		}
		return sonderClient;
	}

	public static void setSonderClient(SonderClient sonderClient) {
		Context.sonderClient = sonderClient;
	}
}
