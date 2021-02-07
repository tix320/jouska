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
import com.github.tix320.jouska.bot.console.CLI;
import com.github.tix320.jouska.bot.console.ConsoleProgressBar;
import com.github.tix320.jouska.bot.console.JoinGameCLICommand;
import com.github.tix320.jouska.bot.console.JoinTournamentCLICommand;
import com.github.tix320.jouska.bot.process.BotProcess;
import com.github.tix320.jouska.bot.service.origin.ApplicationUpdateOrigin;
import com.github.tix320.jouska.bot.service.origin.AuthenticationService;
import com.github.tix320.jouska.bot.service.origin.BotGameManagementOrigin;
import com.github.tix320.jouska.bot.service.origin.BotTournamentOrigin;
import com.github.tix320.jouska.core.Version;
import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.LoginResult;
import com.github.tix320.jouska.core.util.ClassUtils;
import com.github.tix320.nimble.api.OS;
import com.github.tix320.nimble.api.SystemProperties;
import com.github.tix320.skimp.api.check.Try;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.client.rpc.ClientRPCProtocol;
import com.github.tix320.sonder.api.common.communication.CertainReadableByteChannel;

public class BotApp {

	private static SonderClient sonderClient;

	public static void main(String[] args) throws InterruptedException, IOException {
		String processCommand = args[0];
		String nickname = args[1];
		String password = args[2];
		Configuration configuration = new Configuration(
				Path.of(SystemProperties.getUserDirectory(), "jouska-bot", "config.properties"));
		InetSocketAddress serverAddress = configuration.getServerAddress();
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

		Class<?>[] originInterfaces = ClassUtils.getPackageClasses("com.github.tix320.jouska.bot.service.origin");
		Class<?>[] endpointClasses = ClassUtils.getPackageClasses("com.github.tix320.jouska.bot.service.endpoint");

		ClientRPCProtocol rpcProtocol = SonderClient.getRPCProtocolBuilder()
				.registerOriginInterfaces(originInterfaces)
				.registerEndpointClasses(endpointClasses)
				.build();
		sonderClient = SonderClient.forAddress(serverAddress)
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
			Try.run(sonderClient::stop);
			throw new IllegalStateException("Invalid credentials");
		}

		BotProcess botProcess = new BotProcess(processCommand);

		Context.setBotProcess(botProcess);

		CLI cli = new CLI(List.of(new JoinGameCLICommand(rpcProtocol.getOrigin(BotGameManagementOrigin.class)),
				new JoinTournamentCLICommand(rpcProtocol.getOrigin(BotTournamentOrigin.class))));

		cli.run();
	}

	private static void checkApplicationUpdate() throws InterruptedException {
		ApplicationUpdateOrigin applicationUpdateOrigin = Context.getRPCProtocol()
				.getOrigin(ApplicationUpdateOrigin.class);
		Version lastVersion = applicationUpdateOrigin.getVersion().get(Duration.ofSeconds(30));
		int compareResult = lastVersion.compareTo(Version.CURRENT);
		if (compareResult < 0) {
			System.err.printf("Server version - %s, Bot version - %s ", lastVersion, Version.CURRENT);
			System.exit(1);
		}
		if (compareResult > 0) { // update
			System.out.printf("The newer version of bot is available: %s%n", lastVersion);
			System.out.println("Start downloading...");
			applicationUpdateOrigin.downloadBot(OS.CURRENT).waitAndApply(Duration.ofSeconds(30), transfer -> {
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
				} catch (IOException e) {
					try {
						sonderClient.stop();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					throw new RuntimeException(e);
				}
			});
		}
	}
}
