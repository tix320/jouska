package com.github.tix320.jouska.server.infrastructure.application;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.InGamePlayer;
import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.jouska.core.application.game.creation.GameFactory;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.CreateGameCommand;
import com.github.tix320.jouska.core.dto.GameConnectionAnswer;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.dto.GameWatchDto;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.event.PlayerDisconnectedEvent;
import com.github.tix320.jouska.server.event.PlayerLogoutEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.service.GameService;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.MapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.api.util.None;

import static com.github.tix320.jouska.server.app.Services.GAME_ORIGIN;

public class GameManager {
	private static final IDGenerator ID_GENERATOR = new IDGenerator(1);
	private static final MapProperty<Long, GameInfo> games = Property.forMap(new ConcurrentHashMap<>());

	public static long createNewGame(CreateGameCommand createGameCommand, Player creator) {
		long gameId = ID_GENERATOR.next();
		GameSettings gameSettings = createGameCommand.getGameSettings();

		Set<Player> accessedPlayers = createGameCommand.getAccessedPlayers()
				.stream()
				.map(PlayerService::findPlayerByNickname)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());

		games.put(gameId, new GameInfo(gameId, gameSettings, creator, accessedPlayers));

		return gameId;
	}

	public static Observable<Collection<GameInfo>> games(Player caller) {
		return games.asObservable().map(Map::values).map(gameInfos -> {
			if (caller.isAdmin()) {
				return gameInfos;
			}
			else {
				return gameInfos.stream()
						.filter(gameInfo -> hasAccessToGame(gameInfo, caller))
						.collect(Collectors.toList());
			}
		});
	}

	public static GameConnectionAnswer joinGame(long gameId, Player player) {
		AtomicReference<GameConnectionAnswer> answer = new AtomicReference<>(GameConnectionAnswer.GAME_NOT_FOUND);
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			if (!hasAccessToGame(gameInfo, player)) {
				throw new IllegalAccessException();
			}

			if (gameInfo.getGame().isPresent()) {
				answer.set(GameConnectionAnswer.ALREADY_STARTED);
			}

			int playersCount = gameInfo.getSettings().getPlayersCount();
			Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
			if (connectedPlayers.size() < playersCount) { // free
				answer.set(GameConnectionAnswer.CONNECTED);
				connectedPlayers.add(player);
			}
			else {
				answer.set(GameConnectionAnswer.ALREADY_FULL);
			}

			return gameInfo;
		});
		return answer.get();
	}

	public static void leaveGame(long gameId, Player player) {
		games.computeIfPresent(gameId, (key, gameInfo) -> {
			if (!hasAccessToGame(gameInfo, player)) {
				throw new IllegalAccessException();
			}

			if (gameInfo.getGame().isPresent()) {

				gameInfo.getGame().get().kick(player);
			}

			Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();
			connectedPlayers.remove(player);

			return gameInfo;
		});
	}

	public static GameWatchDto watchGame(long gameId) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null || gameInfo.getGame().isEmpty()) {
			throw new IllegalArgumentException(String.format("Game `%s` does not exists", gameId));
		}

		Game game = gameInfo.getGame().orElseThrow();
		return new GameWatchDto(gameId, (TimedGameSettings) gameInfo.getSettings(), game.getPlayers());
	}

	public static void turnInGame(long gameId, Player player, Point point) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null) {
			throw new IllegalStateException(String.format("Game %s not found", gameId));
		}

		Game game = gameInfo.getGame().orElseThrow();
		if (!containsPlayerInGame(game, player)) {
			throw new IllegalStateException(String.format("Player %s is not a player of game %s", player, gameId));
		}

		turnInGame(gameInfo, player, point);
	}

	public static Game getGame(long gameId, Player caller) {
		GameInfo gameInfo = games.get(gameId);
		if (gameInfo == null) {
			throw new IllegalArgumentException(String.format("Game %s not found", gameId));
		}

		if (!hasAccessToGame(gameInfo, caller)) {
			throw new IllegalAccessException();
		}

		return gameInfo.getGame().orElseThrow();
	}

	private static void turnInGame(GameInfo gameInfo, Player player, Point point) {
		Game game = gameInfo.getGame().orElseThrow();

		InGamePlayer currentGamePlayer = game.getCurrentPlayer();

		if (!player.equals(currentGamePlayer.getRealPlayer())) {
			throw new IllegalStateException(
					String.format("Now is turn of %s, not %s", currentGamePlayer.getRealPlayer(), player));
		}

		game.turn(point);
	}

	public static void startGame(long gameId, Player caller) {
		GameInfo gameInfo = games.get(gameId);
		synchronized (gameInfo) {
			if (!gameInfo.getCreator().equals(caller) && !caller.isAdmin()) {
				throw new IllegalAccessException();
			}

			if (gameInfo.getConnectedPlayers().size() != gameInfo.getSettings().getPlayersCount()) { // full
				throw new IllegalStateException("Not fully");
			}

			TimedGameSettings gameSettings = (TimedGameSettings) gameInfo.getSettings();
			Set<Player> connectedPlayers = gameInfo.getConnectedPlayers();

			Game game = GameFactory.create(gameSettings, connectedPlayers);
			gameInfo.setGame(game);

			List<InGamePlayer> gamePlayers = game.getPlayers();

			game.completed().subscribe(ignored -> onGameComplete(gameInfo));

			List<Observable<None>> playersReady = new ArrayList<>();
			for (InGamePlayer player : gamePlayers) {
				ClientPlayerMappingResolver.getClientIdByPlayer(player.getRealPlayer().getId())
						.ifPresentOrElse(clientId -> {
							Observable<None> playerReady = GAME_ORIGIN.notifyGameStarted(
									new GamePlayDto(gameId, gameSettings, gamePlayers, player.getColor()), clientId);
							playersReady.add(playerReady);
						}, () -> logPlayerConnectionNotFound(player.getRealPlayer()));
			}

			EventDispatcher.on(PlayerLogoutEvent.class).takeUntil(game.completed()).subscribe(event -> {
				Player logoutPlayer = event.getPlayer();
				if (containsPlayerInGame(game, logoutPlayer)) {
					game.kick(logoutPlayer);
				}
			});

			EventDispatcher.on(PlayerDisconnectedEvent.class).takeUntil(game.completed()).subscribe(event -> {
				Player disconnectedPlayer = event.getPlayer();
				if (containsPlayerInGame(game, disconnectedPlayer)) {
					game.kick(disconnectedPlayer);
				}
			});

			Observable.zip(playersReady).subscribe(nones -> {
				game.start();
				System.out.println(String.format("Game %s (%s) started", gameSettings.getName(), gameId));
			});
		}
	}

	private static boolean hasAccessToGame(GameInfo game, Player player) {
		if (player.isAdmin() || game.getCreator().equals(player)) {
			return true;
		}

		if (game.getAccessedPlayers().isEmpty()) {
			return true;
		}

		return game.getAccessedPlayers().contains(player);
	}

	private static void onGameComplete(GameInfo gameInfo) {
		long gameId = gameInfo.getId();
		Game game = gameInfo.getGame().orElseThrow();
		GameSettings gameSettings = gameInfo.getSettings();
		InGamePlayer winner = game.getWinner().orElseThrow();
		games.remove(gameId);
		System.out.println(String.format("Game %s(%s) ended: Players %s Winner is %s", gameSettings.getName(), gameId,
				game.getPlayers(), winner));
		GameService.saveGame(game);
	}

	private static boolean containsPlayerInGame(Game game, Player player) {
		for (InGamePlayer gamePlayer : game.getPlayers()) {
			if (player.equals(gamePlayer.getRealPlayer())) {
				return true;
			}
		}
		return false;
	}

	private static void logPlayerConnectionNotFound(Player player) {
		new IllegalStateException(String.format("No connection found for player %s", player)).printStackTrace();
	}
}
