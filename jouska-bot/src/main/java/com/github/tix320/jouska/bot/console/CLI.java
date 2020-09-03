package com.github.tix320.jouska.bot.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.tix320.kiwi.api.util.LoopThread;
import com.github.tix320.kiwi.api.util.Threads;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 26-Apr-20.
 */
public class CLI {

	private final LoopThread thread;

	private final BufferedReader reader;

	private final PrintStream outputWriter;

	private final PrintStream errWriter;

	public CLI(List<CLICommand> commandListeners) {
		Map<String, CLICommand> commandListenersCopy = commandListeners.stream()
				.collect(toMap(CLICommand::name, Function.identity()));
		reader = new BufferedReader(new InputStreamReader(System.in));
		outputWriter = System.out;
		errWriter = System.err;

		this.thread = Threads.createLoopThread(() -> {
			String commandTxt;
			try {
				commandTxt = reader.readLine();
			}
			catch (IOException e) {
				errWriter.println(e.getMessage());
				throw new InterruptedException();
			}

			ParsedCommand parsedCommand;
			try {
				parsedCommand = parseCommand(commandTxt);
			}
			catch (CommandParseException e) {
				errWriter.println(e.getMessage());
				return;
			}

			String commandName = parsedCommand.getName();
			CLICommand CLICommand = commandListenersCopy.get(commandName);
			if (CLICommand == null) {
				errWriter.println("Unknown command: " + commandName);
				return;
			}

			try {
				String result = CLICommand.accept(parsedCommand.getParams());
				outputWriter.println(result);
			}
			catch (CommandException e) {
				errWriter.println(e.getMessage());
			}
		});

		commandListenersCopy.put("exit", new CLICommand() {
			@Override
			public String name() {
				return "exit";
			}

			@Override
			public String accept(Map<String, String> params) {
				thread.stop();
				return "Bye";
			}
		});

		thread.start();
	}

	public void run() {
		outputWriter.println("Hi. I am Jouska Bot");
		thread.start();
	}

	private static ParsedCommand parseCommand(String command) {
		String[] parts = command.strip().split(" ");
		if (parts.length == 0) {
			throw new CommandParseException("Empty command");
		}

		String commandName = parts[0];

		Map<String, String> params = new HashMap<>();

		boolean mustBeParam = true;
		String lastParamName = null;
		for (int i = 1; i < parts.length; i++) {
			String value = parts[i];
			if (mustBeParam) {
				if (!value.startsWith("--")) {
					throw new CommandParseException("Invalid command part: " + value);
				}
				String paramName = value.substring(2);
				params.put(paramName, null);
				lastParamName = paramName;
				mustBeParam = false;
			}
			else {
				if (value.startsWith("--")) {
					String paramName = value.substring(2);
					params.put(paramName, null);
					lastParamName = paramName;
					mustBeParam = false;
				}
				else {
					params.put(lastParamName, value);
					mustBeParam = true;
				}
			}
		}

		return new ParsedCommand(commandName, params);
	}

	public interface CLICommand {

		String name();

		String accept(Map<String, String> params) throws CommandException;
	}

	private static final class ParsedCommand {
		private final String name;
		private final Map<String, String> params;

		private ParsedCommand(String name, Map<String, String> params) {
			this.name = name;
			this.params = params;
		}

		public String getName() {
			return name;
		}

		public Map<String, String> getParams() {
			return params;
		}
	}

	private static final class CommandParseException extends RuntimeException {

		public CommandParseException(String message) {
			super(message);
		}
	}

	public static final class CommandException extends Exception {

		public CommandException(String message) {
			super(message);
		}
	}
}

