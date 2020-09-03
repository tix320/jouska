package com.github.tix320.jouska.bot.console;

import java.time.Duration;
import java.util.Map;

import com.github.tix320.jouska.bot.console.CLI.CLICommand;
import com.github.tix320.jouska.bot.console.CLI.CommandException;
import com.github.tix320.jouska.bot.service.BotGameManagementOrigin;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;

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
				if (response.isSuccess()) {
					return response.getResult().name();
				}
				else {
					return response.getError().getMessage();
				}
			}).get(Duration.ofSeconds(30));
			if (result.equals("CONNECTED")) {
				return "Joined to game: " + gameId;
			}
			else {
				throw new CommandException("Error: " + result);
			}
		}
		catch (TimeoutException | InterruptedException e) {
			throw new CommandException("Server did not respond within 30 seconds");
		}
	}
}
