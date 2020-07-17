package com.github.tix320.jouska.bot.console;

import java.time.Duration;
import java.util.Map;

import com.github.tix320.jouska.bot.console.CLI.CLICommand;
import com.github.tix320.jouska.bot.console.CLI.CommandException;
import com.github.tix320.jouska.bot.service.BotGameManagementOrigin;
import com.github.tix320.jouska.bot.service.BotTournamentOrigin;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;

/**
 * @author Tigran Sargsyan on 05-May-20.
 */
public class JoinCLICommand implements CLICommand {

	private final BotTournamentOrigin botTournamentOrigin;

	private final BotGameManagementOrigin botGameManagementOrigin;

	public JoinCLICommand(BotTournamentOrigin botTournamentOrigin, BotGameManagementOrigin botGameManagementOrigin) {
		this.botTournamentOrigin = botTournamentOrigin;
		this.botGameManagementOrigin = botGameManagementOrigin;
	}

	@Override
	public String name() {
		return "join";
	}

	@Override
	public String accept(Map<String, String> params) throws CommandException {
		String tournamentId = params.get("tournament");
		String gameId = params.get("game");

		if (tournamentId != null && gameId != null) {
			throw new CommandException("Only one of these params must be specified [tournament, game]");
		}
		if (tournamentId != null) {
			try {
				String result = botTournamentOrigin.join(tournamentId).map(response -> {
					if (response.isSuccess()) {
						return response.getResult().name();
					}
					else {
						return response.getError().getMessage();
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
			catch (TimeoutException | InterruptedException e) {
				throw new CommandException("Timeout");
			}
		}
		else if (gameId != null) {
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
				throw new CommandException("Timeout");
			}
		}
		else {
			throw new CommandException("One of these params must be specified [tournament, game]");
		}

	}
}
