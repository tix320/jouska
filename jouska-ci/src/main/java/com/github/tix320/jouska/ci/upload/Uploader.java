package com.github.tix320.jouska.ci.upload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.common.communication.ChannelTransfer;
import com.github.tix320.sonder.api.common.communication.Headers;
import com.github.tix320.sonder.api.common.communication.LimitedReadableByteChannel;

public class Uploader {

	public static void main(String[] args)
			throws IOException {
		String filePath = args[0];
		String os = args[1];
		SonderClient sonderClient = SonderClient.forAddress(new InetSocketAddress("3.230.34.96", 8888))
				.withRPCProtocol(builder -> builder.scanPackages("com.github.tix320.jouska.ci.upload"))
				.withTopicProtocol()
				.build();
		UploaderService uploaderService = sonderClient.getRPCService(UploaderService.class);
		Path file = Path.of(filePath);
		long length = Files.size(file);
		ChannelTransfer transfer = new ChannelTransfer(Headers.EMPTY,
				new LimitedReadableByteChannel(FileChannel.open(file, StandardOpenOption.READ), length));
		switch (os) {
			case "WINDOWS":
				uploaderService.uploadWindows(transfer).subscribe(none -> {
					Try.runOrRethrow(sonderClient::close);
				});
				break;
			case "LINUX":
				uploaderService.uploadLinux(transfer).subscribe(none -> {
					Try.runOrRethrow(sonderClient::close);
				});
				break;
			case "MAC":
				uploaderService.uploadMac(transfer).subscribe(none -> {
					Try.runOrRethrow(sonderClient::close);
				});
				break;
			default:
				throw new IllegalArgumentException(os);
		}
	}
}
