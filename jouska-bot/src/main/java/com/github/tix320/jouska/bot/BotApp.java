package com.github.tix320.jouska.bot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.jouska.core.dto.LoginResult;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.sonder.api.client.SonderClient;
import com.github.tix320.sonder.api.common.communication.CertainReadableByteChannel;

public class BotApp {

	public static SonderClient SONDER_CLIENT;
	public static BotGameManagementOrigin GAME_MANAGEMENT_SERVICE;
	public static BotGameOrigin GAME_SERVICE;
	public static BotTournamentOrigin TOURNAMENT_SERVICE;

	public static JouskaBotProcess BOT;

	public static void main(String[] args) {
		String processCommand = args[0];
		String nickname = args[1];
		String password = args[2];
		String host = Configuration.getServerHost();
		int port = Configuration.getServerPort();


		// checkApplicationUpdate();

		// BOT = new JouskaBotProcess(processCommand);

		// TOURNAMENT_SERVICE = SONDER_CLIENT.getRPCService(BotTournamentOrigin.class);
		//
		// AuthenticationService authenticationService = SONDER_CLIENT.getRPCService(AuthenticationService.class);

		for (int i = 1; i <=6; i++) {
			SonderClient sonderClient = SonderClient.forAddress(new InetSocketAddress(host, port))
					.withRPCProtocol(builder -> builder.scanPackages("com.github.tix320.jouska.bot"))
					.headersTimeoutDuration(Duration.ofSeconds(Integer.MAX_VALUE))
					.contentTimeoutDurationFactory(contentLength -> Duration.ofSeconds(Integer.MAX_VALUE))
					.build();

			String botNickname = "Bot" + i;
			String botPassword = "foo";
			sonderClient.getRPCService(AuthenticationService.class).forceLogin(new LoginCommand(botNickname, botPassword)).subscribe(loginAnswer -> {
				if (loginAnswer.getLoginResult() == LoginResult.SUCCESS) {
					sonderClient.getRPCService(BotTournamentOrigin.class).getTournaments().toMono().subscribe(tournamentViews -> {
						TournamentView tournamentView = tournamentViews.get(0);
						long id = tournamentView.getId();
						sonderClient.getRPCService(BotTournamentOrigin.class).join(id);
					});
				}
				else {
					throw new IllegalStateException(loginAnswer.toString());
				}
			});
		}

		// authenticationService.forceLogin(new LoginCommand(nickname, password)).subscribe(loginAnswer -> {
		// 	if (loginAnswer.getLoginResult() == LoginResult.SUCCESS) {
		// 		TOURNAMENT_SERVICE.getTournaments().toMono().subscribe(tournamentViews -> {
		// 			TournamentView tournamentView = tournamentViews.get(0);
		// 			long id = tournamentView.getId();
		// 			TOURNAMENT_SERVICE.join(id);
		// 		});
		// 	}
		// 	else {
		// 		throw new IllegalStateException(loginAnswer.toString());
		// 	}
		// });
	}

	private static void checkApplicationUpdate() {
		ApplicationUpdateOrigin applicationUpdateOrigin = SONDER_CLIENT.getRPCService(ApplicationUpdateOrigin.class);
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
						ProgressBar progressBar = new ProgressBar();

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
							SONDER_CLIENT.close();
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
