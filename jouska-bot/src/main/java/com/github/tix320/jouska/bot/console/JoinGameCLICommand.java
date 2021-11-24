package com.github.tix320.jouska.bot.console;

import java.time.Duration;
import java.util.Map;

import com.github.tix320.jouska.bot.console.CLI.CLICommand;
import com.github.tix320.jouska.bot.console.CLI.CommandException;
import com.github.tix320.jouska.bot.service.origin.BotGameManagementOrigin;
import com.github.tix320.kiwi.observable.TimeoutException;
import com.github.tix320.skimp.api.exception.ThreadInterruptedException;

/**
 * @author Tigran Sargsyan on 05-May-20.
 */
public class JoinGameCLICommand implements CLICommand {

	private final BotGameManagementOrigin botGameManagementOrigin;

	public JoinGameCLICommand(BotGameManagementOrigin botGameManagementOrigin) {
		this.botGameManagementOrigin = botGameManagementOrigin;
	}

	@Override
	public String name() {
		return "join-game";
	}

	@Override
	public String accept(Map<String, String> params) throws CommandException {
		String gameId = params.get("id");

		if (gameId == null) {
			throw new CommandException("Param `id` must be specified");
		}

		try {
			String result = botGameManagementOrigin.join(gameId).map(response -> {
				try {
					return response.get().name();
				} catch (Exception e) {
					return e.getMessage();
				}
			}).get(Duration.ofSeconds(30));
			if (result.equals("CONNECTED")) {
				return "Joined to game: " + gameId;
			} else {
				throw new CommandException("Error: " + result);
			}
		} catch (TimeoutException | ThreadInterruptedException e) {
			throw new CommandException("Server did not respond within 30 seconds");
		}
	}
}
