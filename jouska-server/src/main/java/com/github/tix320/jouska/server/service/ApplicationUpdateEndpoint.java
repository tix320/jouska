package com.github.tix320.jouska.server.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.jouska.server.app.Application;
import com.github.tix320.sonder.api.common.communication.ChannelTransfer;
import com.github.tix320.sonder.api.common.communication.Headers;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("application")
public class ApplicationUpdateEndpoint {

	private static final String WINDOWS_FILE_NAME = "/jouska-windows.zip";
	private static final String LINUX_FILE_NAME = "/jouska-linux-setup.run";
	private static final String MAC_FILE_NAME = "/jouska-mac-setup.run";

	@Endpoint("check-update")
	public String checkUpdate(String version, String os) {
		String applicationVersion = Application.config.getApplicationVersion();
		Path installersPath = Application.config.getSourcesPath();
		if (installersPath == null) {
			throw new IllegalStateException("Sources path not specified");
		}

		if (version.equals(applicationVersion)) {
			return "";
		}

		switch (os) {
			case "WINDOWS":
				return Files.exists(Path.of(installersPath + WINDOWS_FILE_NAME)) ? applicationVersion : "";
			case "LINUX":
				return Files.exists(Path.of(installersPath + LINUX_FILE_NAME)) ? applicationVersion : "";
			case "MAC":
				return Files.exists(Path.of(installersPath + MAC_FILE_NAME)) ? applicationVersion : "";
			default:
				throw new IllegalArgumentException(os);
		}
	}

	@Endpoint("windows-latest")
	public Transfer downloadWindowsLatest() {
		Path installersPath = Application.config.getSourcesPath();
		return fileToTransfer(installersPath + WINDOWS_FILE_NAME);
	}

	@Endpoint("linux-latest")
	public Transfer downloadLinuxLatest() {
		Path installersPath = Application.config.getSourcesPath();
		return fileToTransfer(installersPath + LINUX_FILE_NAME);
	}

	@Endpoint("mac-latest")
	public Transfer downloadMacLatest() {
		Path installersPath = Application.config.getSourcesPath();
		return fileToTransfer(installersPath + MAC_FILE_NAME);
	}

	@Endpoint("upload-windows")
	public void uploadWindows(Transfer transfer) {
		Path installersPath = Application.config.getSourcesPath();
		transferToFile(transfer, installersPath + WINDOWS_FILE_NAME);
	}

	@Endpoint("upload-linux")
	public void uploadLinux(Transfer transfer) {
		Path installersPath = Application.config.getSourcesPath();
		transferToFile(transfer, installersPath + LINUX_FILE_NAME);
	}

	@Endpoint("upload-mac")
	public void uploadMac(Transfer transfer) {
		Path installersPath = Application.config.getSourcesPath();
		transferToFile(transfer, installersPath + MAC_FILE_NAME);
	}

	private Transfer fileToTransfer(String filePath) {
		try {
			Path file = Path.of(filePath);
			long length = Files.size(file);
			return new ChannelTransfer(Headers.EMPTY, FileChannel.open(file, StandardOpenOption.READ), length);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void transferToFile(Transfer transfer, String filePath) {
		if (transfer.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalStateException();
		}
		int zipLength = (int) transfer.getContentLength();
		int consumedBytes = 0;

		ReadableByteChannel channel = transfer.channel();
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
