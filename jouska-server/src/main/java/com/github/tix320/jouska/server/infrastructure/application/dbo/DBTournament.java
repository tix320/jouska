package com.github.tix320.jouska.server.infrastructure.application.dbo;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.creation.*;
import com.github.tix320.jouska.core.application.tournament.ClassicGroup.GroupPlayer;
import com.github.tix320.jouska.core.application.tournament.*;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.infrastructure.dao.TournamentDao;
import com.github.tix320.jouska.server.infrastructure.entity.*;
import com.github.tix320.jouska.server.infrastructure.helper.Converters;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.property.MapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyMapProperty;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Tigran Sargsyan on 20-Apr-20.
 */
public class DBTournament implements Tournament {

	private static final TournamentDao tournamentDao = new TournamentDao();

	private static final MapProperty<String, DBTournament> tournaments = Property.forMap(new ConcurrentHashMap<>());

	static {
		List<TournamentEntity> tournamentEntities = tournamentDao.findAll();

		tournamentEntities.stream().filter(tournamentEntity -> {
			if (tournamentEntity.getState() == TournamentState.INITIAL) {
				tournamentDao.deleteById(tournamentEntity.getId());
				return false;
			}
			return true;
		}).forEach(DBTournament::fromEntity);
	}

	private final String id;

	private final Player creator;

	private final RestorableTournament tournament;

	public static ReadOnlyMapProperty<String, DBTournament> all() {
		return tournaments.toReadOnly();
	}

	public static DBTournament createNew(DBTournamentSettings settings) {
		Player creator = settings.getCreator();
		RestorableTournament tournament = settings.getWrappedTournamentSettings().createTournament();
		String id = saveTournament(tournament.getPlayers(), settings, creator);
		DBTournament dbTournament = new DBTournament(id, creator, tournament);
		tournaments.put(id, dbTournament);
		return dbTournament;
	}

	public static DBTournament fromEntity(TournamentEntity tournamentEntity) {
		String gameId = tournamentEntity.getId();

		return tournaments.computeIfAbsent(gameId, id1 -> {
			RestorableTournament tournament = buildTournamentFromEntity(tournamentEntity);
			Player creator = Converters.playerEntityToPLayer(tournamentEntity.getCreator());

			List<Group> groups = tournamentEntity.getGroups()
					.stream()
					.map(groupEntity -> restoreGroupFromEntity(creator, groupEntity))
					.collect(Collectors.toList());


			PlayOff playOff = tournamentEntity.getPlayOff() == null ? null :
					restorePlayOffFromEntity(creator, tournamentEntity.getPlayOff());

			tournament.restore(groups, playOff);

			DBTournament dbTournament = new DBTournament(tournamentEntity.getId(), creator, tournament);
			dbTournament.addListeners();
			return dbTournament;
		});
	}

	private DBTournament(String id, Player creator, RestorableTournament tournament) {
		this.id = id;
		this.creator = creator;
		this.tournament = tournament;
	}

	@Override
	public TournamentSettings getSettings() {
		return tournament.getSettings();
	}

	@Override
	public void addPlayer(Player player) {
		synchronized (tournament) {
			tournament.addPlayer(player);
			updateTournamentPlayers();
		}
	}

	@Override
	public boolean removePlayer(Player player) {
		synchronized (tournament) {
			boolean removed = tournament.removePlayer(player);
			if (removed) {
				updateTournamentPlayers();
			}
			return removed;
		}
	}

	@Override
	public void start() {
		synchronized (tournament) {
			tournament.start();
			updateTournamentStart();
			addListeners();
		}
	}

	@Override
	public Set<Player> getPlayers() {
		return tournament.getPlayers();
	}

	@Override
	public List<RestorableGroup> getGroups() {
		return tournament.getGroups();
	}

	@Override
	public ReadOnlyProperty<RestorablePlayOff> playOff() {
		return tournament.playOff();
	}

	@Override
	public TournamentState getState() {
		return tournament.getState();
	}

	@Override
	public MonoObservable<DBTournament> completed() {
		return tournament.completed().map(t -> this).toMono();
	}

	public String getId() {
		return id;
	}

	public Player getCreator() {
		return creator;
	}

	private void addListeners() {
		tournament.getGroups()
				.stream()
				.flatMap(group -> group.getGames().stream())
				.forEach(game -> game.completed().subscribe(o -> updateTournamentGroups()));

		tournament.playOff()
				.asObservable()
				.subscribe(playOff -> playOff.getTours().subscribe(lists -> updateTournamentPlayOff(playOff)));

		tournament.completed().subscribe(ignored -> updateCompletedTournament());
	}

	private static String saveTournament(Set<Player> players, DBTournamentSettings settings, Player creator) {
		Set<PlayerEntity> playerEntities = players.stream()
				.map(player -> new PlayerEntity(player.getId()))
				.collect(toSet());

		RestorableTournamentSettings restorableTournamentSettings = settings.extractPureSettings();

		TournamentEntity tournamentEntity = new TournamentEntity(new PlayerEntity(creator.getId()),
				restorableTournamentSettings, playerEntities, Collections.emptyList(), null, TournamentState.INITIAL);

		return tournamentDao.save(tournamentEntity);
	}

	private void updateTournamentPlayers() {
		Set<PlayerEntity> playerEntities = tournament.getPlayers()
				.stream()
				.map(player -> new PlayerEntity(player.getId()))
				.collect(toSet());

		TournamentEntity tournamentEntity = new TournamentEntity(id);
		tournamentEntity.setPlayers(playerEntities);

		tournamentDao.update(tournamentEntity, Map.of("players", TournamentEntity::getPlayers));
	}

	private void updateTournamentStart() {
		Set<PlayerEntity> playerEntities = tournament.getPlayers()
				.stream()
				.map(player -> new PlayerEntity(player.getId()))
				.collect(toSet());

		List<GroupEntity> groupEntities = convertGroupToEntity(tournament.getGroups());

		TournamentEntity tournamentEntity = new TournamentEntity(id);
		tournamentEntity.setState(TournamentState.GROUP_STAGE);
		tournamentEntity.setGroups(groupEntities);
		tournamentEntity.setPlayers(playerEntities);

		tournamentDao.update(tournamentEntity,
				Map.of("state", TournamentEntity::getState, "players", TournamentEntity::getPlayers, "groups",
						TournamentEntity::getGroups));
	}

	private void updateTournamentGroups() {
		List<GroupEntity> groupEntities = convertGroupToEntity(tournament.getGroups());

		TournamentEntity tournamentEntity = new TournamentEntity(id);
		tournamentEntity.setGroups(groupEntities);

		tournamentDao.update(tournamentEntity, Map.of("groups", TournamentEntity::getGroups));
	}

	private void updateTournamentPlayOff(RestorablePlayOff playOff) {
		PlayOffEntity playOffEntity = convertPlayOffToEntity(playOff);

		TournamentEntity tournamentEntity = new TournamentEntity(id);
		tournamentEntity.setState(TournamentState.PLAY_OFF_STAGE);
		tournamentEntity.setPlayOff(playOffEntity);

		tournamentDao.update(tournamentEntity,
				Map.of("state", TournamentEntity::getState, "playOff", TournamentEntity::getPlayOff));
	}

	private void updateCompletedTournament() {
		PlayOffEntity playOffEntity = convertPlayOffToEntity(tournament.playOff().getValue());

		TournamentEntity tournamentEntity = new TournamentEntity(id);
		tournamentEntity.setPlayOff(playOffEntity);
		tournamentEntity.setState(TournamentState.COMPLETED);

		tournamentDao.update(tournamentEntity,
				Map.of("playOff", TournamentEntity::getPlayOff, "state", TournamentEntity::getState));
	}

	private static RestorableTournament buildTournamentFromEntity(TournamentEntity tournamentEntity) {
		Player creator = Converters.playerEntityToPLayer(tournamentEntity.getCreator());
		Set<Player> players = Converters.playerEntityToPLayer(tournamentEntity.getPlayers());

		switch (tournamentEntity.getState()) {
			case GROUP_STAGE:
			case PLAY_OFF_STAGE:
			case COMPLETED:
				RestorableTournamentSettings settings = tournamentEntity.getSettings();
				settings = DBTournamentSettings.wrap(settings, creator).getWrappedTournamentSettings();

				RestorableTournament tournament = settings.createTournament();

				players.forEach(tournament::addPlayer);

				return tournament;
			default:
				throw new IllegalStateException();
		}
	}

	private static RestorableGroup restoreGroupFromEntity(Player creator, GroupEntity groupEntity) {
		Set<Player> players = Converters.playerEntityToPLayer(groupEntity.getPlayers());

		List<Game> games = groupEntity.getGames().stream().map(DBGame::fromEntity).collect(Collectors.toList());

		RestorableGroupSettings settings = groupEntity.getSettings();
		GameSettings baseGameSettings = settings.getBaseGameSettings();
		settings = settings.changeBaseGameSettings(
				new DBGameSettings(creator, Collections.emptySet(), (RestorableGameSettings) baseGameSettings));

		RestorableGroup group = settings.createGroup(players);
		group.restore(games);
		return group;
	}

	private static PlayOff restorePlayOffFromEntity(Player creator, PlayOffEntity playOffEntity) {
		List<Player> players = playOffEntity.getPlayers()
				.stream()
				.map(Converters::playerEntityToPLayer)
				.collect(toList());

		List<List<PlayOffGameEntity>> entityTours = playOffEntity.getTours();

		List<List<PlayOffGame>> tours = entityTours.stream()
				.map(playOffGameEntities -> playOffGameEntities.stream()
						.map(playOffGameEntity -> new PlayOffGame(
								Converters.playerEntityToPLayer(playOffGameEntity.getFirstPlayer()),
								Converters.playerEntityToPLayer(playOffGameEntity.getSecondPlayer()),
								playOffGameEntity.getGame() == null ? null :
										DBGame.fromEntity(playOffGameEntity.getGame())))
						.collect(Collectors.toList()))
				.collect(Collectors.toList());

		RestorablePlayOffSettings settings = playOffEntity.getSettings();

		GameSettings baseGameSettings = settings.getBaseGameSettings();
		settings = settings.changeBaseGameSettings(
				new DBGameSettings(creator, Collections.emptySet(), (RestorableGameSettings) baseGameSettings));

		RestorablePlayOff playOff = settings.createPlayOff(players);
		playOff.restore(tours);

		return playOff;
	}

	private static List<GroupEntity> convertGroupToEntity(List<RestorableGroup> groups) {
		return groups.stream().map(DBTournament::convertGroupToEntity).collect(toList());
	}

	private static GroupEntity convertGroupToEntity(RestorableGroup group) {
		List<PlayerEntity> playerEntities = convertGroupPlayerToEntity(group.getPlayers());

		List<GameEntity> gameEntities = group.getGames()
				.stream()
				.map(game -> (DBGame) game)
				.map(game -> new GameEntity(game.getId()))
				.collect(toList());

		RestorableGroupSettings settings = group.getSettings();

		GameSettings baseGameSettings = settings.getBaseGameSettings();
		baseGameSettings = ((DBGameSettings) baseGameSettings).getWrappedGameSettings();

		return new GroupEntity(settings.changeBaseGameSettings(baseGameSettings), new HashSet<>(playerEntities),
				gameEntities);
	}

	private static PlayOffEntity convertPlayOffToEntity(RestorablePlayOff playOff) {
		List<PlayerEntity> playerEntities = convertPlayerToEntity(playOff.getPlayers());

		List<List<PlayOffGameEntity>> playOffGames = playOff.getTours()
				.get(Duration.ZERO)
				.stream()
				.map(tourGames -> tourGames.stream().map(DBTournament::convertPlayOffGameToEntity).collect(toList()))
				.collect(toList());

		RestorablePlayOffSettings settings = playOff.getSettings();

		GameSettings baseGameSettings = settings.getBaseGameSettings();
		baseGameSettings = ((DBGameSettings) baseGameSettings).getWrappedGameSettings();

		return new PlayOffEntity(settings.changeBaseGameSettings(baseGameSettings), playerEntities, playOffGames);
	}

	private static PlayOffGameEntity convertPlayOffGameToEntity(PlayOffGame playOffGame) {
		PlayerEntity firstPlayerEntity = convertPlayerToEntity(playOffGame.getFirstPlayer());
		PlayerEntity secondPlayerEntity = convertPlayerToEntity(playOffGame.getSecondPlayer());

		GameEntity gameEntity = new GameEntity(((DBGame) playOffGame.getGame()).getId());

		return new PlayOffGameEntity(firstPlayerEntity, secondPlayerEntity, gameEntity);
	}

	private static PlayerEntity convertPlayerToEntity(Player player) {
		if (player == null) {
			return null;
		}

		return new PlayerEntity(player.getId());
	}

	private static List<PlayerEntity> convertPlayerToEntity(List<Player> players) {
		return players.stream().map(player -> new PlayerEntity(player.getId())).collect(toList());
	}

	private static List<PlayerEntity> convertGroupPlayerToEntity(List<GroupPlayer> players) {
		return players.stream().map(groupPlayer -> new PlayerEntity(groupPlayer.getPlayer().getId())).collect(toList());
	}

	@Override
	public Object getLock() {
		return tournament.getLock();
	}
}
