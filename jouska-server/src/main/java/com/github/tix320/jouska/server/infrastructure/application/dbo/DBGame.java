package com.github.tix320.jouska.server.infrastructure.application.dbo;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.app.AppConfig;
import com.github.tix320.jouska.server.infrastructure.dao.GameDao;
import com.github.tix320.jouska.server.infrastructure.entity.GameEntity;
import com.github.tix320.jouska.server.infrastructure.entity.GamePlayerEntity;
import com.github.tix320.jouska.server.infrastructure.entity.GameStatisticsEntity;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import com.github.tix320.jouska.server.infrastructure.helper.Converters;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.property.MapProperty;
import com.github.tix320.kiwi.property.Property;
import com.github.tix320.kiwi.property.ReadOnlyMapProperty;
import com.github.tix320.kiwi.property.Stock;
import dev.morphia.query.experimental.filters.Filters;

import static java.util.stream.Collectors.toMap;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
public class DBGame implements Game {

	private static final MapProperty<String, DBGame> games = Property.forMap(new ConcurrentHashMap<>());

	private static final GameDao gameDao = AppConfig.INJECTOR.inject(GameDao.class);

	static {
		List<GameEntity> notCompletedGames = gameDao.findAll(Filters.ne("state", GameState.COMPLETED));

		notCompletedGames.stream().peek(gameEntity -> {
			if (gameEntity.getState() == GameState.RUNNING) {
				gameEntity.setState(GameState.INITIAL);
				gameEntity.setChanges(null);
				gameEntity.setStatistics(null);
				gameDao.update(gameEntity);
			}
		}).forEach(DBGame::fromEntity);
	}

	private final String id;

	private final Player creator;

	private final Set<Player> accessedPlayers;

	private final RestorableGame game;

	public static ReadOnlyMapProperty<String, DBGame> all() {
		return games.toReadOnly();
	}

	public static DBGame createNew(Player creator, Set<Player> accessedPlayers, RestorableGameSettings gameSettings) {
		RestorableGame game = gameSettings.createGame();
		String id = saveGameInDB(game, creator, accessedPlayers);
		DBGame dbGame = new DBGame(id, creator, accessedPlayers, game);
		games.put(id, dbGame);
		return dbGame;
	}

	public static DBGame fromEntity(GameEntity gameEntity) {
		String gameId = gameEntity.getId();

		return games.computeIfAbsent(gameId, id1 -> {
			RestorableGame game = buildGameFromEntity(gameEntity);
			Player creator = Converters.playerEntityToPLayer(gameEntity.getCreator());
			Set<Player> accessedPlayers = Converters.playerEntityToPLayer(gameEntity.getAccessedPlayers());

			DBGame dbGame = new DBGame(gameEntity.getId(), creator, accessedPlayers, game);

			if (gameEntity.getState() == GameState.COMPLETED) {
				game.restore(gameEntity.getChanges());
				game.completed().subscribe(gameObj -> dbGame.updateCompletedGameInDB());
			}

			return dbGame;
		});
	}

	private DBGame(String id, Player creator, Set<Player> accessedPlayers, RestorableGame game) {
		this.id = id;
		this.creator = creator;
		this.accessedPlayers = accessedPlayers;
		this.game = game;
	}

	@Override
	public GameSettings getSettings() {
		return game.getSettings();
	}

	@Override
	public void addPlayer(GamePlayer player) {
		synchronized (getLock()) {
			game.addPlayer(player);
			updatePlayersInDB();
		}
	}

	@Override
	public boolean removePlayer(Player player) {
		synchronized (getLock()) {
			boolean removed = game.removePlayer(player);
			if (removed) {
				updatePlayersInDB();
			}
			return removed;
		}
	}

	@Override
	public void shufflePLayers() {
		game.shufflePLayers();
		updatePlayersInDB();
	}

	@Override
	public void start() {
		synchronized (getLock()) {
			game.start();
			updateRunningInDB();
			game.completed().subscribe(gameObj -> updateCompletedGameInDB());
		}
	}

	@Override
	public ReadOnlyGameBoard getBoard() {
		return game.getBoard();
	}

	@Override
	public CellChange turn(Point point) {
		return game.turn(point);
	}

	@Override
	public Stock<GameChange> changes() {
		return game.changes();
	}

	@Override
	public List<Point> getPointsBelongedToPlayer(Player player) {
		return game.getPointsBelongedToPlayer(player);
	}

	@Override
	public List<Player> getPlayers() {
		return game.getPlayers();
	}

	@Override
	public List<GamePlayer> getGamePlayers() {
		return game.getGamePlayers();
	}

	@Override
	public List<GamePlayer> getActivePlayers() {
		return game.getActivePlayers();
	}

	@Override
	public GamePlayer getCurrentPlayer() {
		return game.getCurrentPlayer();
	}

	@Override
	public Optional<GamePlayer> ownerOfPoint(Point point) {
		return game.ownerOfPoint(point);
	}

	@Override
	public Statistics getStatistics() {
		return game.getStatistics();
	}

	@Override
	public List<GamePlayer> getLosers() {
		return game.getLosers();
	}

	@Override
	public Optional<GamePlayer> getWinner() {
		return game.getWinner();
	}

	@Override
	public List<PlayerWithPoints> getKickedPlayers() {
		return game.getKickedPlayers();
	}

	@Override
	public PlayerWithPoints kick(Player player) {
		return game.kick(player);
	}

	@Override
	public void forceCompleteGame(Player winner) {
		game.forceCompleteGame(winner);
	}

	@Override
	public GameState getState() {
		return game.getState();
	}

	@Override
	public boolean isStarted() {
		return game.isStarted();
	}

	@Override
	public boolean isCompleted() {
		return game.isCompleted();
	}

	@Override
	public MonoObservable<DBGame> completed() {
		return game.completed().map(g -> this).toMono();
	}

	public String getId() {
		return id;
	}

	public Player getCreator() {
		return creator;
	}

	public Set<Player> getAccessedPlayers() {
		return accessedPlayers;
	}

	private static String saveGameInDB(RestorableGame game, Player creator, Set<Player> accessedPlayers) {
		List<GamePlayerEntity> playerEntities = game.getGamePlayers()
				.stream()
				.map(player -> new GamePlayerEntity(new PlayerEntity(player.getRealPlayer().getId()),
						player.getColor()))
				.collect(Collectors.toList());

		Set<PlayerEntity> accessedPlayerEntities = accessedPlayers.stream()
				.map(player -> new PlayerEntity(player.getId()))
				.collect(Collectors.toSet());
		GameEntity gameEntity = new GameEntity(new PlayerEntity(creator.getId()), accessedPlayerEntities,
				game.getSettings(), GameState.INITIAL, playerEntities, Collections.emptyList(),
				new GameStatisticsEntity(Collections.emptyMap()));

		return gameDao.save(gameEntity);
	}

	private void updatePlayersInDB() {
		List<GamePlayerEntity> playerEntities = game.getGamePlayers()
				.stream()
				.map(player -> new GamePlayerEntity(new PlayerEntity(player.getRealPlayer().getId()),
						player.getColor()))
				.collect(Collectors.toList());

		GameEntity gameEntity = new GameEntity(id);
		gameEntity.setPlayers(playerEntities);

		gameDao.update(gameEntity, List.of("players"));
	}

	private void updateRunningInDB() {
		GameEntity gameEntity = new GameEntity(id);
		gameEntity.setState(GameState.RUNNING);

		gameDao.update(gameEntity, List.of("state"));
	}

	private void updateCompletedGameInDB() {
		List<GameChange> changes = game.changes().list();

		GameStatisticsEntity gameStatistics = new GameStatisticsEntity(
				adaptStatistics(game.getStatistics().summaryPoints()));

		GameEntity gameEntity = new GameEntity(id);
		gameEntity.setState(GameState.COMPLETED);
		gameEntity.setChanges(changes);
		gameEntity.setStatistics(gameStatistics);

		gameDao.update(gameEntity, List.of("state", "changes", "statistics"));
	}

	private static Map<PlayerColor, Integer> adaptStatistics(Map<GamePlayer, Integer> statistics) {
		return statistics.entrySet().stream().collect(toMap(entry -> entry.getKey().getColor(), Entry::getValue));
	}

	private static RestorableGame buildGameFromEntity(GameEntity gameEntity) {
		RestorableGameSettings settings = gameEntity.getSettings();

		GameState state = gameEntity.getState();

		RestorableGame game = settings.createGame();
		gameEntity.getPlayers().stream().map(Converters::gamePlayerEntityToInGamePlayer).forEach(game::addPlayer);

		return switch (state) {
			case INITIAL, COMPLETED -> game;
			default -> throw new IllegalStateException();
		};
	}

	@Override
	public Object getLock() {
		return game.getLock();
	}
}
