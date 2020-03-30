package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.app.Configuration;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.CallerUser;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.Role;
import com.github.tix320.sonder.api.common.communication.*;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("application")
public class ApplicationUpdateEndpoint {

	private static final String WINDOWS_FILE_NAME = "/jouska-windows.zip";
	private static final String LINUX_FILE_NAME = "/jouska-linux-setup.sh";
	private static final String MAC_FILE_NAME = "/jouska-mac-setup.sh";

	@Endpoint("check-update")
	public String checkUpdate(String version, String os) {
		String applicationVersion = Configuration.getApplicationVersion();
		Path clientAppPath = Configuration.getClientAppPath();
		if (clientAppPath == null) {
			throw new IllegalStateException("Sources path not specified");
		}

		if (version.equals(applicationVersion)) {
			return "";
		}

		switch (os) {
			case "WINDOWS":
				return Files.exists(Path.of(clientAppPath + WINDOWS_FILE_NAME)) ? applicationVersion : "";
			case "LINUX":
				return Files.exists(Path.of(clientAppPath + LINUX_FILE_NAME)) ? applicationVersion : "";
			case "MAC":
				return Files.exists(Path.of(clientAppPath + MAC_FILE_NAME)) ? applicationVersion : "";
			default:
				throw new IllegalArgumentException(os);
		}
	}

	@Endpoint("windows-latest")
	public Transfer downloadWindowsLatest() {
		Path installersPath = Configuration.getClientAppPath();
		return fileToTransfer(installersPath + WINDOWS_FILE_NAME);
	}

	@Endpoint("linux-latest")
	public Transfer downloadLinuxLatest() {
		Path installersPath = Configuration.getClientAppPath();
		return fileToTransfer(installersPath + LINUX_FILE_NAME);
	}

	@Endpoint("mac-latest")
	public Transfer downloadMacLatest() {
		Path installersPath = Configuration.getClientAppPath();
		return fileToTransfer(installersPath + MAC_FILE_NAME);
	}

	@Endpoint("upload-windows")
	@Role(RoleName.ADMIN)
	public void uploadWindows(Transfer transfer, @CallerUser Player player) {
		Path installersPath = Configuration.getClientAppPath();
		transferToFile(transfer, installersPath + WINDOWS_FILE_NAME);
	}

	@Endpoint("upload-linux")
	@Role(RoleName.ADMIN)
	public void uploadLinux(Transfer transfer, @CallerUser Player player) {
		Path installersPath = Configuration.getClientAppPath();
		transferToFile(transfer, installersPath + LINUX_FILE_NAME);
	}

	@Endpoint("upload-mac")
	@Role(RoleName.ADMIN)
	public void uploadMac(Transfer transfer, @CallerUser Player player) {
		Path installersPath = Configuration.getClientAppPath();
		transferToFile(transfer, installersPath + MAC_FILE_NAME);
	}

	private Transfer fileToTransfer(String filePath) {
		try {
			Path file = Path.of(filePath);
			long length = Files.size(file);
			return new ChannelTransfer(Headers.EMPTY,
					new LimitedReadableByteChannel(FileChannel.open(file, StandardOpenOption.READ), length));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
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
