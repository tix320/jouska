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

public class BotApp {

	public static void main(String[] args) throws InterruptedException, IOException {
		String processCommand = args[0];
		String nickname = args[1];
		String password = args[2];
		String host = Configuration.getServerHost();
		int port = Configuration.getServerPort();

		// for (int i = 1; i <= 15; i++) {
		// 	SonderClient sonderClient = SonderClient.forAddress(new InetSocketAddress(host, port))
		// 			.withRPCProtocol(builder -> builder.scanPackages("com.github.tix320.jouska.bot"))
		// 			.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(100))
		// 			.build();
		//
		// 	String botNickname = "Bot" + i;
		// 	String botPassword = "foo";
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
		// }
		//
		// if (true) {
		// 	return;
		// }

		SonderClient sonderClient = SonderClient.forAddress(new InetSocketAddress(host, port))
				.withRPCProtocol(builder -> builder.scanPackages("com.github.tix320.jouska.bot.service"))
				.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(100))
				.build();

		sonderClient.connect();

		Context.setSonderClient(sonderClient);

		checkApplicationUpdate();

		LoginAnswer answer = sonderClient.getRPCService(AuthenticationService.class)
				.forceLogin(new Credentials(nickname, password))
				.get(Duration.ofSeconds(15));

		if (answer.getLoginResult() != LoginResult.SUCCESS) {
			Try.run(sonderClient::close);
			throw new IllegalStateException("Invalid credentials");
		}

		BotProcess botProcess = new BotProcess(processCommand);

		Context.setBotProcess(botProcess);

		CLI cli = new CLI(List.of(new JoinCLICommand(sonderClient.getRPCService(BotTournamentOrigin.class),
				sonderClient.getRPCService(BotGameManagementOrigin.class))));

		cli.run();
	}

	private static void checkApplicationUpdate() {
		ApplicationUpdateOrigin applicationUpdateOrigin = Context.getSonderClient()
				.getRPCService(ApplicationUpdateOrigin.class);
		applicationUpdateOrigin.checkUpdate(Version.VERSION, Version.os).subscribe(lastVersion -> {
			if (!lastVersion.equals("")) { // update
				System.out.println(String.format("The newer version of bot is available. %s", lastVersion));
				System.out.println("Start downloading...");
				applicationUpdateOrigin.downloadBot(Version.os).subscribe(transfer -> {
					String fileName = "jouska-bot-" + lastVersion + ".zip";

					CertainReadableByteChannel channel = transfer.channel();
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
							progressBar.tick((long) Math.ceil(progress));
						}

						System.out.println(String.format("New bot zip successfully download, use it. %s", fileName));
						System.exit(0);
					}
					catch (IOException e) {
						try {
							Context.getSonderClient().close();
						}
						catch (IOException ex) {
							ex.printStackTrace();
						}
						throw new RuntimeException(e);
					}

				});
			}
		});
	}
}
