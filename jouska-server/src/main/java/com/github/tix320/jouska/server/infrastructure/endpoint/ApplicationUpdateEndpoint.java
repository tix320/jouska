package com.github.tix320.jouska.server.infrastructure.endpoint;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.deft.api.OS;
import com.github.tix320.jouska.core.Version;
import com.github.tix320.jouska.server.app.Configuration;
import com.github.tix320.sonder.api.common.communication.ChannelTransfer;
import com.github.tix320.sonder.api.common.communication.Headers;
import com.github.tix320.sonder.api.common.communication.StaticTransfer;
import com.github.tix320.sonder.api.common.communication.Transfer;
import com.github.tix320.sonder.api.common.communication.channel.LimitedReadableByteChannel;
import com.github.tix320.sonder.api.common.rpc.Endpoint;

@Endpoint("application")
public class ApplicationUpdateEndpoint {

	private static final String WINDOWS_CLIENT_FILE_NAME = "/jouska-client-windows.zip";
	private static final String WINDOWS_BOT_FILE_NAME = "/jouska-bot-windows.zip";

	private static final String LINUX_CLIENT_FILE_NAME = "/jouska-client-linux.zip";
	private static final String LINUX_BOT_FILE_NAME = "/jouska-bot-linux.zip";

	private static final String MAC_CLIENT_FILE_NAME = "/jouska-client-mac.zip";
	private static final String MAC_BOT_FILE_NAME = "/jouska-bot-mac.zip";

	private final Configuration configuration;

	public ApplicationUpdateEndpoint(Configuration configuration) {
		this.configuration = configuration;
	}

	@Endpoint
	public Version getVersion() {
		return Version.CURRENT;
	}

	@Endpoint
	public Transfer downloadClient(OS os) {
		Path clientAppPath = configuration.getClientAppPath();
		return switch (os) {
			case WINDOWS -> fileToTransfer(clientAppPath + WINDOWS_CLIENT_FILE_NAME);
			case LINUX -> fileToTransfer(clientAppPath + LINUX_CLIENT_FILE_NAME);
			case MAC -> fileToTransfer(clientAppPath + MAC_CLIENT_FILE_NAME);
			default -> throw new IllegalArgumentException();
		};
	}

	@Endpoint
	public Transfer downloadBot(OS os) {
		Path clientAppPath = configuration.getClientAppPath();
		return switch (os) {
			case WINDOWS -> fileToTransfer(clientAppPath + WINDOWS_BOT_FILE_NAME);
			case LINUX -> fileToTransfer(clientAppPath + LINUX_BOT_FILE_NAME);
			case MAC -> fileToTransfer(clientAppPath + MAC_BOT_FILE_NAME);
			default -> throw new IllegalArgumentException();
		};
	}

	private Transfer fileToTransfer(String filePath) {
		try {
			Path file = Path.of(filePath);
			long length = Files.size(file);
			FileChannel fileChannel = FileChannel.open(file, StandardOpenOption.READ);
			LimitedReadableByteChannel channel = new LimitedReadableByteChannel(fileChannel, length);
			channel.completeness().subscribe(none -> {
				try {
					fileChannel.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			return new ChannelTransfer(Headers.builder().header("ready", true).build(), channel);
		} catch (NoSuchFileException e) {
			return new StaticTransfer(Headers.builder().header("ready", false).build(), new byte[0]);
		} catch (IOException e) {
			e.printStackTrace();
			return new StaticTransfer(Headers.builder().header("ready", false).build(), new byte[0]);
		}
	}
}
