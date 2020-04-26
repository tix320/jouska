package com.github.tix320.jouska.bot.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.github.tix320.jouska.core.util.LoopThread;

/**
 * @author Tigran Sargsyan on 26-Apr-20.
 */
public class CLI {

	private final LoopThread thread;

	private final BufferedReader reader;

	private final PrintStream outputWriter;

	private final PrintStream errWriter;

	public CLI(Map<String, CommandListener> commandListeners) {
		Map<String, CommandListener> commandListenersCopy = new HashMap<>(commandListeners);
		reader = new BufferedReader(new InputStreamReader(System.in));
		outputWriter = System.out;
		errWriter = System.err;

		this.thread = new LoopThread(() -> {
			String commandTxt;
			try {
				commandTxt = reader.readLine();
			}
			catch (IOException e) {
				errWriter.println(e.getMessage());
				return false;
			}

			Command command;
			try {
				command = parseCommand(commandTxt);
			}
			catch (CommandParseException e) {
				errWriter.println(e.getMessage());
				return true;
			}

			String commandName = command.getName();
			CommandListener commandListener = commandListenersCopy.get(commandName);
			if (commandListener == null) {
				errWriter.println("Unknown command: " + commandName);
				return true;
			}

			try {
				String result = commandListener.doThis(command.getParams());
				outputWriter.println(result);
			}
			catch (CommandException e) {
				errWriter.println(e.getMessage());
			}

			return true;
		}, false);

		commandListenersCopy.put("exit", params ->

		{
			thread.stop();
			return "Bye";
		});
	}

	public void run() {
		outputWriter.println("Hi. I am Jouska Bot");
		thread.start();
	}

	private static Command parseCommand(String command) {
		String[] parts = command.split(" ");
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

		return new Command(commandName, params);
	}

	public interface CommandListener {

		String doThis(Map<String, String> params);
	}

	private static final class Command {
		private final String name;
		private final Map<String, String> params;

		private Command(String name, Map<String, String> params) {
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

	public static final class CommandException extends RuntimeException {

		public CommandException(String message) {
			super(message);
		}
	}
}

