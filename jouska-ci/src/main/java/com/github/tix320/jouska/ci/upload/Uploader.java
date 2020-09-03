package com.github.tix320.jouska.ci.upload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.common.communication.ChannelTransfer;
import com.github.tix320.sonder.api.common.communication.Headers;
import com.github.tix320.sonder.api.common.communication.LimitedReadableByteChannel;
import com.github.tix320.sonder.api.common.rpc.RPCProtocol;

public class Uploader {

	public static void main(String[] args) throws IOException, InterruptedException {
		String filePath = args[0];
		String os = args[1];


		RPCProtocol rpcProtocol = RPCProtocol.forClient()
				.scanOriginPackages("com.github.tix320.jouska.ci.upload")
				.build();
		SonderClient sonderClient = SonderClient.forAddress(new InetSocketAddress("52.57.98.213", 8888))
				.registerProtocol(rpcProtocol)
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(10000))
				.build();

		sonderClient.connect();

		try {
			rpcProtocol.getOrigin(AuthenticationService.class)
					.forceLogin(new Credentials("uploader", "uploader"))
					.get(Duration.ofSeconds(30));
		}
		catch (TimeoutException e) {
			sonderClient.close();
			return;
		}

		UploaderService uploaderService = rpcProtocol.getOrigin(UploaderService.class);
		Path file = Path.of(filePath);
		long length = Files.size(file);
		ChannelTransfer transfer = new ChannelTransfer(Headers.EMPTY,
				new LimitedReadableByteChannel(FileChannel.open(file, StandardOpenOption.READ), length));
		switch (os) {
			case "WINDOWS":
				uploaderService.uploadWindowsClient(transfer).subscribe(none -> Try.runOrRethrow(sonderClient::close));
				break;
			case "LINUX":
				uploaderService.uploadLinuxClient(transfer).subscribe(none -> Try.runOrRethrow(sonderClient::close));
				break;
			case "MAC":
				uploaderService.uploadMacClient(transfer).subscribe(none -> Try.runOrRethrow(sonderClient::close));
				break;
			default:
				throw new IllegalArgumentException(os);
		}
	}
}
