package com.github.tix320.jouska.core.update;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleConsumer;

import com.github.tix320.deft.api.SystemProperties;
import com.github.tix320.jouska.core.Version;
import com.github.tix320.jouska.core.util.ChannelUtils;
import com.github.tix320.jouska.core.util.ZipUtils;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.communication.channel.FiniteReadableByteChannel;

public class UpdateRunner {

	private final Path appHomeDirectory;

	private final Version updateVersion;

	private final DoubleConsumer progressHandler;

	public UpdateRunner(Path appHomeDirectory, Version updateVersion, DoubleConsumer progressHandler) {
		this.appHomeDirectory = appHomeDirectory;
		this.updateVersion = updateVersion;
		this.progressHandler = progressHandler;
	}

	public void update(Transfer updateTransfer) throws UpdateNotReadyException, IOException {
		final long currentPid = ProcessHandle.current().pid();
		final Path appDirectoryPath = SystemProperties.CURRENT_JAVA_HOME;
		final Path updatePath = appHomeDirectory.resolve(Path.of("update"));
		final Path zipPath = updatePath.resolve(updateVersion.toString() + ".zip");
		final Path outPath = updatePath.resolve("out");
		final Path javaExecutablePath = outPath.resolve(Path.of("bin", "java"));

		boolean ready = updateTransfer.headers().getNonNullBoolean("ready");
		if (!ready) {
			throw new UpdateNotReadyException();
		}

		Files.createDirectories(outPath);

		try (FiniteReadableByteChannel channel = updateTransfer.contentChannel();
			 FileChannel fileChannel = FileChannel.open(zipPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

			ChannelUtils.pipe(channel, fileChannel, channel.getContentLength(), progressHandler);

			ZipUtils.unzip(zipPath, outPath);

			final ProcessBuilder processBuilder = new ProcessBuilder(javaExecutablePath.toString(), "-m",
					"jouska.core/com.github.tix320.jouska.core.update.Updater", appDirectoryPath.toString(),
					zipPath.toString(), String.valueOf(currentPid));

			final Process process = processBuilder.start();

			CompletableFuture.runAsync(() -> {
				try {
					Thread.sleep(30000);
					process.destroyForcibly();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			});

			final PrintWriter bufferedWriter = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);

			bufferedWriter.println("START");
			bufferedWriter.flush();

			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			final String line = bufferedReader.readLine();
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
			if (line.equals("READY")) {
				System.exit(0);
			} else {
				throw new IllegalStateException("Illegal message: " + line);
			}
		}
	}
}
