package com.github.tix320.jouska.bot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.List;

import com.github.tix320.jouska.bot.config.Configuration;
import com.github.tix320.jouska.bot.config.Version;
import com.github.tix320.jouska.bot.console.CLI;
import com.github.tix320.jouska.bot.console.ConsoleProgressBar;
import com.github.tix320.jouska.bot.console.JoinCLICommand;
import com.github.tix320.jouska.bot.process.BotProcess;
import com.github.tix320.jouska.bot.service.ApplicationUpdateOrigin;
import com.github.tix320.jouska.bot.service.AuthenticationService;
import com.github.tix320.jouska.bot.service.BotGameManagementOrigin;
import com.github.tix320.jouska.bot.service.BotTournamentOrigin;
import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.LoginResult;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.common.communication.CertainReadableByteChannel;
import com.github.tix320.sonder.api.common.rpc.RPCProtocol;

public class BotApp {

	private static SonderClient sonderClient;

	public static void main(String[] args) throws InterruptedException, IOException {
		String processCommand = args[0];
		String nickname = args[1];
		String password = args[2];
		String host = Configuration.getServerHost();
		int port = Configuration.getServerPort();

		// for (int i = 1; i <= 5; i++) {
		// 	SonderClient sonderClient = SonderClient.forAddress(new InetSocketAddress(host, port))
		// 			.withRPCProtocol(builder -> builder.scanPackages("com.github.tix320.jouska.bot"))
		// 			.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(10000))
		// 			.build();
		//
		// 	String botNickname = "Bot" + i;
		// 	String botPassword = "foo";
		//
		// 	sonderClient.connect();
		//
		// 	Context.setSonderClient(sonderClient);
		//
		// 	sonderClient.getRPCService(AuthenticationService.class)
		// 			.forceLogin(new Credentials(botNickname, botPassword))
		// 			.subscribe(loginAnswer -> {
		// 				if (loginAnswer.getLoginResult() == LoginResult.SUCCESS) {
		// 					sonderClient.getRPCService(BotTournamentOrigin.class)
		// 							.getTournaments()
		// 							.toMono()
		// 							.subscribe(tournamentViews -> {
		// 								TournamentView tournamentView = tournamentViews.stream()
		// 										.filter(tournamentView1 -> !tournamentView1.isStarted())
		// 										.findFirst()
		// 										.orElseThrow(() -> new IllegalStateException(
		// 												"No any free tournament found"));
		// 								String id = tournamentView.getId();
		// 								sonderClient.getRPCService(BotTournamentOrigin.class).join(id);
		// 							});
		// 				}
		// 				else {
		// 					throw new IllegalStateException(loginAnswer.toString());
		// 				}
		// 			});
		//
		//
		// }
		//
		// if (true) {
		// 	return;
		// }

		RPCProtocol rpcProtocol = RPCProtocol.forClient()
				.scanOriginPackages("com.github.tix320.jouska.bot.service")
				.scanEndpointPackages("com.github.tix320.jouska.bot.service")
				.build();
		sonderClient = SonderClient.forAddress(new InetSocketAddress(host, port))
				.registerProtocol(rpcProtocol)
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(10000))
				.build();

		sonderClient.connect();

		Context.setRpcProtocol(rpcProtocol);

		checkApplicationUpdate();

		LoginAnswer answer = rpcProtocol.getOrigin(AuthenticationService.class)
				.forceLogin(new Credentials(nickname, password))
				.get(Duration.ofSeconds(15));

		if (answer.getLoginResult() != LoginResult.SUCCESS) {
			Try.run(sonderClient::close);
			throw new IllegalStateException("Invalid credentials");
		}

		BotProcess botProcess = new BotProcess(processCommand);

		Context.setBotProcess(botProcess);

		CLI cli = new CLI(List.of(new JoinCLICommand(rpcProtocol.getOrigin(BotTournamentOrigin.class),
				rpcProtocol.getOrigin(BotGameManagementOrigin.class))));

		cli.run();
	}

	private static void checkApplicationUpdate() throws InterruptedException {
		ApplicationUpdateOrigin applicationUpdateOrigin = Context.getRPCProtocol()
				.getOrigin(ApplicationUpdateOrigin.class);
		String lastVersion = applicationUpdateOrigin.checkUpdate(Version.VERSION, Version.os)
				.get(Duration.ofSeconds(30));
		if (!lastVersion.equals("")) { // update
			System.out.printf("The newer version of bot is available: %s%n", lastVersion);
			System.out.println("Start downloading...");
			applicationUpdateOrigin.downloadBot(Version.os).waitAndApply(Duration.ofSeconds(30), transfer -> {
				boolean ready = transfer.getHeaders().getNonNullBoolean("ready");
				if (!ready) {
					throw new IllegalStateException("Unable to download now");
				}

				CertainReadableByteChannel channel = transfer.channel();
				String fileName = "jouska-bot-" + lastVersion + ".zip";
				long zipLength = channel.getContentLength();
				int consumedBytes = 0;

				try (FileChannel fileChannel = FileChannel.open(Path.of(fileName), StandardOpenOption.CREATE,
						StandardOpenOption.WRITE)) {
					ConsoleProgressBar progressBar = new ConsoleProgressBar();

					ByteBuffer buffer = ByteBuffer.allocate(1024 * 64);
					int read;
					while ((read = channel.read(buffer)) != -1) {
						buffer.flip();
						fileChannel.write(buffer);
						buffer.clear();
						consumedBytes += read;
						final double progress = (double) consumedBytes / zipLength;
						progressBar.tick(progress);
					}
					System.out.println();

					System.out.printf("New bot zip successfully download, use it: %s%n", fileName);
					System.exit(0);
				}
				catch (IOException e) {
					try {
						sonderClient.close();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
					throw new RuntimeException(e);
				}
			});
		}
	}
}
