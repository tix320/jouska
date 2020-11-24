package com.github.tix320.jouska.bot;

import com.github.tix320.jouska.bot.process.BotProcess;
import com.github.tix320.sonder.api.client.rpc.ClientRPCProtocol;

/**
 * @author Tigran Sargsyan on 09-May-20.
 */
public class Context {

	private static ClientRPCProtocol rpcProtocol;

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

	public static ClientRPCProtocol getRPCProtocol() {
		if (rpcProtocol == null) {
			throw new IllegalStateException();
		}
		return rpcProtocol;
	}

	public static void setRpcProtocol(ClientRPCProtocol rpcProtocol) {
		Context.rpcProtocol = rpcProtocol;
	}
}
