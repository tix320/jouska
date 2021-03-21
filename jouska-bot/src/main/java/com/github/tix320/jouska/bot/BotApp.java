package com.github.tix320.jouska.bot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import com.github.tix320.deft.api.OS;
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
import com.github.tix320.jouska.core.update.UpdateNotReadyException;
import com.github.tix320.jouska.core.update.UpdateRunner;
import com.github.tix320.jouska.core.util.ClassUtils;
import com.github.tix320.kiwi.api.reactive.observable.TimeoutException;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.client.rpc.ClientRPCProtocol;
import com.github.tix320.sonder.api.common.communication.Transfer;

public class BotApp {

	public static void main(String[] args) throws InterruptedException, IOException {
		System.out.println("Version: " + Version.CURRENT);

		String processCommand = args[0];
		String nickname = args[1];
		String password = args[2];
		Configuration configuration = new Configuration(AppProperties.APP_CONFIG_FILE);
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

		ClientRPCProtocol rpcProtocol = ClientRPCProtocol.builder()
				.registerOriginInterfaces(originInterfaces)
				.registerEndpointClasses(endpointClasses)
				.build();
		SonderClient sonderClient = SonderClient.forAddress(serverAddress).registerProtocol(rpcProtocol).build();

		sonderClient.start();

		Context.setRpcProtocol(rpcProtocol);

		checkApplicationUpdate();

		LoginAnswer answer = rpcProtocol.getOrigin(AuthenticationService.class)
				.forceLogin(new Credentials(nickname, password))
				.get(Duration.ofSeconds(15));

		if (answer.getLoginResult() != LoginResult.SUCCESS) {
			sonderClient.stop();
			throw new IllegalStateException("Invalid credentials");
		}

		BotProcess botProcess = new BotProcess(processCommand);

		Context.setBotProcess(botProcess);

		CLI cli = new CLI(List.of(new JoinGameCLICommand(rpcProtocol.getOrigin(BotGameManagementOrigin.class)),
				new JoinTournamentCLICommand(rpcProtocol.getOrigin(BotTournamentOrigin.class))));

		cli.run();
	}

	private static void checkApplicationUpdate() {
		ApplicationUpdateOrigin applicationUpdateOrigin = Context.getRPCProtocol()
				.getOrigin(ApplicationUpdateOrigin.class);
		Version lastVersion = applicationUpdateOrigin.getVersion().get(Duration.ofSeconds(30));
		int compareResult = lastVersion.compareTo(Version.CURRENT);
		if (compareResult < 0) {
			final String error = "Illegal state: Bot version higher than server version. Server - %s, Bot - %s.".formatted(
					lastVersion, Version.CURRENT);
			System.err.println(error);
			System.exit(1);
		}
		if (compareResult > 0) { // update
			System.out.printf("The newer version of bot is available: %s%n", lastVersion);

			final Transfer transfer;
			try {
				transfer = applicationUpdateOrigin.downloadBot(OS.CURRENT).get(Duration.ofSeconds(30));
			} catch (TimeoutException e) {
				System.err.println("Timeout");
				System.exit(1);
				return;
			}

			ConsoleProgressBar progressBar = new ConsoleProgressBar();

			UpdateRunner updateRunner = new UpdateRunner(AppProperties.APP_HOME_DIRECTORY, lastVersion,
					progressBar::tick);


			System.out.println("Start downloading...");

			try {
				updateRunner.update(transfer);
			} catch (UpdateNotReadyException e) {
				System.err.println("Update not available now");
				System.exit(1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
