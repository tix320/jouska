package com.github.tix320.jouska.core.update;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.github.tix320.deft.api.SystemProperties;
import com.github.tix320.jouska.core.util.ZipUtils;

public class Updater {

	private static final Path LOG_FILE_PATH = SystemProperties.TEMP_DIRECTORY.resolve("jouska-updater.log");

	public static void main(String[] args) throws IOException {
		PrintWriter logger = new PrintWriter(new OutputStreamWriter(new FileOutputStream(LOG_FILE_PATH.toFile())),
				true);

		try {
			if (args.length == 0) {
				throw new IllegalArgumentException("App directory argument is required");
			}

			final Path appDirectory = Path.of(args[0]);

			if (!Files.isDirectory(appDirectory)) {
				throw new IllegalArgumentException("%s not a directory".formatted(appDirectory));
			}

			final Path appZipFile = Path.of(args[1]);

			if (!Files.isRegularFile(appZipFile)) {
				throw new IllegalArgumentException("%s not a file".formatted(appZipFile));
			}

			logger.println("App Directory: " + appDirectory);
			logger.println("App Zip file: " + appZipFile);

			ProcessHandle process = null;
			if (args.length > 2) {
				final long pid = Long.parseLong(args[2]);

				logger.println("App pid: " + pid);

				process = ProcessHandle.of(pid).orElseThrow(() -> {
					throw new NoSuchElementException("Process with pid %s not found".formatted(pid));
				});
			}

			if (process != null) {
				Scanner scanner = new Scanner(System.in);

				logger.println("Reading START...");
				final String line = scanner.nextLine();

				if (line.equals("START")) {
					logger.println("Writing READY...");
					System.out.println("READY");

					process.onExit().get(30, TimeUnit.SECONDS);
					logger.println("App process exited.");
				} else {
					throw new IllegalStateException("Invalid stdin: %s".formatted(line));
				}
			}

			logger.println("Unzipping...");
			ZipUtils.unzip(appZipFile, appDirectory);
			logger.println("Finish...");

			logger.flush();
		} catch (Throwable e) {
			e.printStackTrace(logger);
			logger.flush();
		}
	}
}
