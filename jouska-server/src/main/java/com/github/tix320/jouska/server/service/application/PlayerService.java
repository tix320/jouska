package com.github.tix320.jouska.server.service.application;

import java.util.Optional;

import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.LoginCommand;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.app.DataSource;
import com.github.tix320.jouska.server.entity.PlayerEntity;
import com.github.tix320.kiwi.api.util.collection.BiConcurrentHashMap;
import com.github.tix320.kiwi.api.util.collection.BiMap;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;

import static com.github.tix320.jouska.server.app.Services.AUTHENTICATION_SERVICE;

public class PlayerService {

	private final static BiMap<Long, String> playerAndClientIds = new BiConcurrentHashMap<>();

	public static synchronized LoginAnswer login(long clientId, LoginCommand loginCommand) {
		return findPlayerByCredentials(loginCommand).map(playerEntity -> {
			if (playerAndClientIds.inverseView().containsKey(playerEntity.getId().toHexString())) {
				return LoginAnswer.ALREADY_LOGGED;
			}
			else {
				playerAndClientIds.put(clientId, playerEntity.getId().toHexString());
				return LoginAnswer.SUCCESS;
			}
		}).orElse(LoginAnswer.INVALID_CREDENTIALS);
	}

	public static synchronized void logout(long clientId) {
		playerAndClientIds.straightRemove(clientId);
	}

	public static synchronized LoginAnswer forceLogin(long clientId, LoginCommand loginCommand) {
		return findPlayerByCredentials(loginCommand).map(playerEntity -> {
			String playerId = playerEntity.getId().toHexString();
			Long existingClientId = playerAndClientIds.inverseView().get(playerId);
			if (existingClientId != null) {
				playerAndClientIds.inverseRemove(playerId);
				AUTHENTICATION_SERVICE.logout(existingClientId);
			}
			playerAndClientIds.put(clientId, playerId);
			return LoginAnswer.SUCCESS;
		}).orElse(LoginAnswer.INVALID_CREDENTIALS);
	}

	public static synchronized Player getPlayerByClientId(long clientId) {
		String playerId = playerAndClientIds.straightView().get(clientId);
		if (playerId == null) {
			throw new NotLoggedException(String.format("Client with id `%s` not logged.", clientId));
		}

		return findPlayerById(playerId).map(
				playerEntity -> new Player(playerEntity.getId().toHexString(), playerEntity.getNickname(),
						playerEntity.getRole())).orElseThrow();
	}

	public static synchronized long getClientIdByPlayer(String playerId) {
		Long clientId = playerAndClientIds.inverseView().get(playerId);
		if (clientId == null) {
			throw new NotLoggedException(
					String.format("Player with id `%s` does not have active logged session.", playerId));
		}
		return clientId;
	}

	private static Optional<PlayerEntity> findPlayerByCredentials(LoginCommand loginCommand) {
		Query<PlayerEntity> query = DataSource.INSTANCE.find(PlayerEntity.class);
		query.and(query.criteria("nickname").equal(loginCommand.getNickname()),
				query.criteria("password").equal(loginCommand.getPassword()));

		return Optional.ofNullable(query.first());
	}

	private static Optional<PlayerEntity> findPlayerById(String playerId) {
		return Optional.ofNullable(DataSource.INSTANCE.get(PlayerEntity.class, new ObjectId(playerId)));
	}

}
