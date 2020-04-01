package com.github.tix320.jouska.server.infrastructure.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.jouska.core.dto.LoginResult;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.entity.PlayerEntity;
import com.github.tix320.jouska.server.event.PlayerLoginEvent;
import com.github.tix320.jouska.server.event.PlayerLogoutEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.NotAuthenticatedException;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;

import static com.github.tix320.jouska.server.app.Services.AUTHENTICATION_ORIGIN;

public class PlayerService {

	public static LoginAnswer login(long clientId, LoginCommand loginCommand) {
		Optional<PlayerEntity> playerByCredentials = findPlayerByCredentials(loginCommand);
		if (playerByCredentials.isEmpty()) {
			return new LoginAnswer(LoginResult.INVALID_CREDENTIALS, null);
		}

		PlayerEntity playerEntity = playerByCredentials.get();

		Optional<Long> clientIdByPlayer = ClientPlayerMappingResolver.getClientIdByPlayer(
				playerEntity.getId().toHexString());

		if (clientIdByPlayer.isPresent()) {
			return new LoginAnswer(LoginResult.ALREADY_LOGGED, entityToModel(playerEntity));
		}
		else {
			ClientPlayerMappingResolver.setMapping(clientId, playerEntity.getId().toHexString());

			Player player = entityToModel(playerEntity);
			EventDispatcher.fire(new PlayerLoginEvent(player));
			return new LoginAnswer(LoginResult.SUCCESS, player);
		}
	}

	public static LoginAnswer forceLogin(long clientId, LoginCommand loginCommand) {
		Optional<PlayerEntity> playerByCredentials = findPlayerByCredentials(loginCommand);
		if (playerByCredentials.isEmpty()) {
			return new LoginAnswer(LoginResult.INVALID_CREDENTIALS, null);
		}

		PlayerEntity playerEntity = playerByCredentials.get();

		String playerId = playerEntity.getId().toHexString();

		Long existingClientId = ClientPlayerMappingResolver.removeByPlayerId(playerId);
		if (existingClientId != null) {
			AUTHENTICATION_ORIGIN.logout(existingClientId);
			EventDispatcher.fire(new PlayerLogoutEvent(entityToModel(playerEntity)));
		}

		ClientPlayerMappingResolver.setMapping(clientId, playerId);
		Player player = entityToModel(playerEntity);
		EventDispatcher.fire(new PlayerLoginEvent(player));
		return new LoginAnswer(LoginResult.SUCCESS, player);
	}


	public static void logout(long clientId) {
		String playerId = ClientPlayerMappingResolver.removeByClientId(clientId);
		if (playerId == null) {
			throw new NotAuthenticatedException(String.format("Client `%s` not authenticated yet", clientId));
		}
		PlayerEntity player = findPlayerEntityById(playerId).orElseThrow();
		EventDispatcher.fire(new PlayerLogoutEvent(entityToModel(player)));
	}

	private static Optional<PlayerEntity> findPlayerByCredentials(LoginCommand loginCommand) {
		Query<PlayerEntity> query = DataSource.INSTANCE.find(PlayerEntity.class);
		query.and(query.criteria("nickname").equal(loginCommand.getNickname()),
				query.criteria("password").equal(loginCommand.getPassword()));

		return Optional.ofNullable(query.first());
	}

	public static Optional<Player> findPlayerById(String playerId) {
		return findPlayerEntityById(playerId).map(PlayerService::entityToModel);
	}

	public static Optional<Player> findPlayerByNickname(String nickname) {
		Query<PlayerEntity> query = DataSource.INSTANCE.find(PlayerEntity.class);
		query.and(query.criteria("nickname").equal(nickname));

		return Optional.ofNullable(query.first()).map(PlayerService::entityToModel);
	}

	public static Optional<PlayerEntity> findPlayerEntityById(String playerId) {
		return Optional.ofNullable(DataSource.INSTANCE.get(PlayerEntity.class, new ObjectId(playerId)));
	}

	private static Player entityToModel(PlayerEntity entity) {
		return new Player(entity.getId().toHexString(), entity.getNickname(), entity.getRole());
	}


	public static Observable<Set<Player>> getConnectedPlayers() {
		return ClientPlayerMappingResolver.getConnectedPlayers()
				.map(data -> data.values()
						.stream()
						.map(PlayerService::findPlayerById)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.collect(Collectors.toSet()));
	}
}
