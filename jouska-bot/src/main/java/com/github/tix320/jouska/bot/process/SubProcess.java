package com.github.tix320.jouska.bot.process;

import java.io.*;
import java.nio.channels.ClosedChannelException;

/**
 * @author Tigran Sargsyan on 02-Apr-20.
 */
public final class SubProcess {

	private final BufferedReader reader;

	private final BufferedWriter writer;

	public SubProcess(String processRunCommand) {
		ProcessBuilder processBuilder = new ProcessBuilder(processRunCommand.split(" "));
		Process process;
		try {
			process = processBuilder.start();
			String messagePrefix = "Process " + process.pid() + ": ";
			System.out.println(messagePrefix + "Started");
			process.onExit().thenRunAsync(() -> System.out.println(messagePrefix + "End"));
			new Thread(() -> {
				BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				try {
					String line;
					while (true) {
						if ((line = errorStream.readLine()) != null) {
							System.err.println(messagePrefix + line);
						}
						break;
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}

			}).start();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
	}

	public void write(String s) {
		try {
			writer.write(s);
			writer.flush();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeLn(String s) {
		try {
			writer.write(s);
			writer.write("\n");
			writer.flush();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String readLine() {
		try {
			String line = reader.readLine();
			if (line == null) {
				throw new ClosedChannelException();
			}
			return line;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
