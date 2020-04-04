package com.github.tix320.jouska.bot;

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
		Process process = null;
		try {
			process = processBuilder.start();
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
