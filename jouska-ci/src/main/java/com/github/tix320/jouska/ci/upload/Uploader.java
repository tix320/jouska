package com.github.tix320.jouska.ci.upload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.sonder.api.client.Clonder;
import com.github.tix320.sonder.api.common.communication.ChannelTransfer;
import com.github.tix320.sonder.api.common.communication.Headers;

public class Uploader {

	public static void main(String[] args)
			throws IOException {
		String filePath = args[0];
		String os = args[1];
		Clonder clonder = Clonder.forAddress(new InetSocketAddress("3.230.34.96", 8888))
				.withRPCProtocol("com.github.tix320.jouska.ci.upload")
				.withTopicProtocol()
				.build();
		UploaderService uploaderService = clonder.getRPCService(UploaderService.class);
		Path file = Path.of(filePath);
		long length = Files.size(file);
		ChannelTransfer transfer = new ChannelTransfer(Headers.EMPTY, FileChannel.open(file, StandardOpenOption.READ),
				length);
		switch (os) {
			case "WINDOWS":
				uploaderService.uploadWindows(transfer).subscribe(none -> {
					Try.runOrRethrow(clonder::close);
				});
				break;
			case "LINUX":
				uploaderService.uploadLinux(transfer).subscribe(none -> {
					Try.runOrRethrow(clonder::close);
				});
				break;
			case "MAC":
				uploaderService.uploadMac(transfer).subscribe(none -> {
					Try.runOrRethrow(clonder::close);
				});
				break;
			default:
				throw new IllegalArgumentException(os);
		}
	}
}
