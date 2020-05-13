package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.jouska.core.infrastructure.OS;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.app.Configuration;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.Role;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.sonder.api.common.communication.*;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("application")
public class ApplicationUpdateEndpoint {

	private static final String WINDOWS_CLIENT_FILE_NAME = "/jouska-client-windows.zip";
	private static final String WINDOWS_BOT_FILE_NAME = "/jouska-bot-windows.zip";

	private static final String LINUX_CLIENT_FILE_NAME = "/jouska-client-linux.zip";
	private static final String LINUX_BOT_FILE_NAME = "/jouska-bot-linux.zip";

	private static final String MAC_CLIENT_FILE_NAME = "/jouska-client-mac.zip";
	private static final String MAC_BOT_FILE_NAME = "/jouska-bot-mac.zip";

	@Endpoint
	public String checkUpdate(String version, OS os) {
		String applicationVersion = Configuration.getApplicationVersion();
		Path clientAppPath = Configuration.getClientAppPath();

		if (version.equals(applicationVersion)) {
			return "";
		}

		return applicationVersion;
	}

	@Endpoint
	public Transfer downloadClient(OS os) {
		Path clientAppPath = Configuration.getClientAppPath();
		switch (os) {
			case WINDOWS:
				return fileToTransfer(clientAppPath + WINDOWS_CLIENT_FILE_NAME);
			case LINUX:
				return fileToTransfer(clientAppPath + LINUX_CLIENT_FILE_NAME);
			case MAC:
				return fileToTransfer(clientAppPath + MAC_CLIENT_FILE_NAME);
			default:
				throw new IllegalArgumentException();
		}
	}

	@Endpoint
	public Transfer downloadBot(OS os) {
		Path clientAppPath = Configuration.getClientAppPath();
		switch (os) {
			case WINDOWS:
				return fileToTransfer(clientAppPath + WINDOWS_BOT_FILE_NAME);
			case LINUX:
				return fileToTransfer(clientAppPath + LINUX_BOT_FILE_NAME);
			case MAC:
				return fileToTransfer(clientAppPath + MAC_BOT_FILE_NAME);
			default:
				throw new IllegalArgumentException();
		}
	}

	@Endpoint
	@Role(RoleName.ADMIN)
	public void uploadWindowsClient(Transfer transfer, @CallerUser Player player) {
		Path clientAppPath = Configuration.getClientAppPath();
		transferToFile(transfer, clientAppPath + WINDOWS_CLIENT_FILE_NAME);
	}

	@Endpoint
	@Role(RoleName.ADMIN)
	public void uploadLinuxClient(Transfer transfer, @CallerUser Player player) {
		Path clientAppPath = Configuration.getClientAppPath();
		transferToFile(transfer, clientAppPath + LINUX_CLIENT_FILE_NAME);
	}

	@Endpoint
	@Role(RoleName.ADMIN)
	public void uploadMacClient(Transfer transfer, @CallerUser Player player) {
		Path clientAppPath = Configuration.getClientAppPath();
		transferToFile(transfer, clientAppPath + MAC_CLIENT_FILE_NAME);
	}

	private Transfer fileToTransfer(String filePath) {
		try {
			Path file = Path.of(filePath);
			long length = Files.size(file);
			FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ);
			LimitedReadableByteChannel channel = new LimitedReadableByteChannel(fileChannel, length);
			channel.onFinish().subscribe(none -> Try.runOrRethrow(fileChannel::close));
			return new ChannelTransfer(Headers.builder().header("ready", true).build(), channel);
		}
		catch (NoSuchFileException e) {
			return new StaticTransfer(Headers.builder().header("ready", false).build(), new byte[0]);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new StaticTransfer(Headers.builder().header("ready", false).build(), new byte[0]);
		}
	}

	private void transferToFile(Transfer transfer, String filePath) {
		CertainReadableByteChannel channel = transfer.channel();
		if (channel.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalStateException();
		}
		int zipLength = (int) channel.getContentLength();
		int consumedBytes = 0;

		double border = 0.1;
		try (FileChannel fileChannel = FileChannel.open(Path.of(filePath), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE)) {
			System.out.println("Uploading started");
			ByteBuffer buffer = ByteBuffer.allocate(zipLength);
			while (buffer.hasRemaining()) {
				int position = buffer.position();
				int read = channel.read(buffer);
				buffer.flip();
				buffer.position(position);
				while (buffer.hasRemaining()) {
					fileChannel.write(buffer);
				}
				buffer.limit(buffer.capacity());
				consumedBytes += read;
				final double progress = (double) consumedBytes / zipLength;
				if (progress > border) {
					System.out.println("Uploading: " + progress);
					border += 0.1;
				}
			}
			System.out.println("Successfully uploaded");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
