package com.gitlab.tixtix320.jouska.server.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.gitlab.tixtix320.jouska.server.app.Application;
import com.gitlab.tixtix320.sonder.api.common.communication.ChannelTransfer;
import com.gitlab.tixtix320.sonder.api.common.communication.Headers;
import com.gitlab.tixtix320.sonder.api.common.communication.Transfer;
import com.gitlab.tixtix320.sonder.api.common.rpc.Endpoint;

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
		long zipLength = transfer.getContentLength();
		int consumedBytes = 0;

		ReadableByteChannel channel = transfer.channel();
		try (FileChannel fileChannel = FileChannel.open(Path.of(installersPath + "/latest.zip"),
				StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ByteBuffer buffer = ByteBuffer.allocate(1024 * 64);
			int read;
			while ((read = channel.read(buffer)) != -1) {
				buffer.flip();
				fileChannel.write(buffer);
				buffer.clear();
				consumedBytes += read;
				final double progress = (double) consumedBytes / zipLength;
				System.out.println("Uploading: " + progress);
			}
			System.out.println("Successfully uploaded");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
