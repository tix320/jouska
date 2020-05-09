package com.github.tix320.jouska.bot.process;

import java.io.*;
import java.lang.ProcessBuilder.Redirect;
import java.nio.channels.ClosedChannelException;

/**
 * @author Tigran Sargsyan on 02-Apr-20.
 */
public final class ProcessCommunicator {

	private final BufferedReader reader;

	private final BufferedWriter writer;

	public ProcessCommunicator(String processRunCommand) {
		ProcessBuilder processBuilder = new ProcessBuilder(processRunCommand.split(" "));
		processBuilder.redirectError(Redirect.INHERIT);
		Process process;
		try {
			process = processBuilder.start();
			System.out.println("Started sub-process with id: " + process.pid());
			process.onExit().thenRunAsync(() -> {
				System.out.println("Sub-process ended: " + process.pid());
			});
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
