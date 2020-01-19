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
public class ApplicationSourcesEndpoint {

	@Endpoint("latest-version")
	public String getLatestVersion() {
		return Application.config.getApplicationVersion();
	}

	@Endpoint("latest-zip")
	public Transfer getApplicationLatestSourcesZip() {
		Path installersPath = Application.config.getSourcesPath();
		if (installersPath == null) {
			throw new IllegalStateException("Sources path not specified");
		}
		else {
			try {
				Path file = Path.of(installersPath + "/jouska.zip");
				long length = Files.size(file);
				return new ChannelTransfer(Headers.EMPTY, FileChannel.open(file, StandardOpenOption.READ), length);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Endpoint("upload")
	public void upload(Transfer transfer) {
		Path installersPath = Application.config.getSourcesPath();
		if (installersPath == null) {
			throw new IllegalStateException("Sources path not specified");
		}
		if (transfer.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalStateException();
		}
		int zipLength = (int) transfer.getContentLength();
		int consumedBytes = 0;

		ReadableByteChannel channel = transfer.channel();
		double border = 0.1;
		try (FileChannel fileChannel = FileChannel.open(Path.of(installersPath + "/jouska.zip"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
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
