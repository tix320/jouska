package com.github.tix320.jouska.bot.console;

import java.time.Duration;
import java.util.Map;

import com.github.tix320.jouska.bot.console.CLI.CLICommand;
import com.github.tix320.jouska.bot.console.CLI.CommandException;
import com.github.tix320.jouska.bot.service.origin.BotTournamentOrigin;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.skimp.api.exception.ThreadInterruptedException;

/**
 * @author Tigran Sargsyan on 05-May-20.
 */
public class JoinTournamentCLICommand implements CLICommand {

	private final BotTournamentOrigin botTournamentOrigin;

	public JoinTournamentCLICommand(BotTournamentOrigin botTournamentOrigin) {
		this.botTournamentOrigin = botTournamentOrigin;
	}

	@Override
	public String name() {
		return "join-tournament";
	}

	@Override
	public String accept(Map<String, String> params) throws CommandException {
		String tournamentId = params.get("id");

		if (tournamentId == null) {
			throw new CommandException("Param `id` must be specified");
		}
		try {
			String result = botTournamentOrigin.join(tournamentId).map(response -> {
				try {
					return response.get().name();
				} catch (Exception e) {
					return e.getMessage();
				}
			}).get(Duration.ofSeconds(30));
			if (result.equals("ACCEPT")) {
				return "Joined to tournament: " + tournamentId;
			}
			else if (result.equals("REJECT")) {
				throw new CommandException("Join rejected");
			}
			else {
				throw new CommandException("Error from server: " + result);
			}
		}
		catch (TimeoutException | ThreadInterruptedException e) {
			throw new CommandException("Server did not respond within 30 seconds");
		}
	}
}
