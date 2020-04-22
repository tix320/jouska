package com.github.tix320.jouska.server.infrastructure.service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.tix320.jouska.core.dto.Credentials;
import com.github.tix320.jouska.core.dto.LoginAnswer;
import com.github.tix320.jouska.core.dto.LoginResult;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.event.PlayerLoginEvent;
import com.github.tix320.jouska.server.event.PlayerLogoutEvent;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.dao.PlayerDao;
import com.github.tix320.jouska.server.infrastructure.endpoint.auth.NotAuthenticatedException;
import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

import static com.github.tix320.jouska.server.app.Services.AUTHENTICATION_ORIGIN;

public class PlayerService {

	private final PlayerDao playerDao;

	public PlayerService() {
		playerDao = new PlayerDao();
	}

	public LoginAnswer login(long clientId, Credentials credentials) {
		Optional<PlayerEntity> playerByCredentials = playerDao.findPlayerByCredentials(credentials);
		if (playerByCredentials.isEmpty()) {
			return new LoginAnswer(LoginResult.INVALID_CREDENTIALS, null);
		}

		PlayerEntity playerEntity = playerByCredentials.get();

		Optional<Long> clientIdByPlayer = ClientPlayerMappingResolver.getClientIdByPlayer(playerEntity.getId());

		if (clientIdByPlayer.isPresent()) {
			return new LoginAnswer(LoginResult.ALREADY_LOGGED, convertEntityToModel(playerEntity));
		}
		else {
			ClientPlayerMappingResolver.setMapping(clientId, playerEntity.getId());

			Player player = convertEntityToModel(playerEntity);
			EventDispatcher.fire(new PlayerLoginEvent(player));
			return new LoginAnswer(LoginResult.SUCCESS, player);
		}
	}

	public LoginAnswer forceLogin(long clientId, Credentials credentials) {
		Optional<PlayerEntity> playerByCredentials = playerDao.findPlayerByCredentials(credentials);
		if (playerByCredentials.isEmpty()) {
			return new LoginAnswer(LoginResult.INVALID_CREDENTIALS, null);
		}

		PlayerEntity playerEntity = playerByCredentials.get();

		String playerId = playerEntity.getId();

		Long existingClientId = ClientPlayerMappingResolver.removeByPlayerId(playerId);
		if (existingClientId != null) {
			AUTHENTICATION_ORIGIN.logout(existingClientId);
			EventDispatcher.fire(new PlayerLogoutEvent(convertEntityToModel(playerEntity)));
		}

		ClientPlayerMappingResolver.setMapping(clientId, playerId);
		Player player = convertEntityToModel(playerEntity);
		EventDispatcher.fire(new PlayerLoginEvent(player));
		return new LoginAnswer(LoginResult.SUCCESS, player);
	}


	public void logout(long clientId) {
		String playerId = ClientPlayerMappingResolver.removeByClientId(clientId);
		if (playerId == null) {
			throw new NotAuthenticatedException(String.format("Client `%s` not authenticated yet", clientId));
		}
		PlayerEntity player = playerDao.findById(playerId).orElseThrow();
		EventDispatcher.fire(new PlayerLogoutEvent(convertEntityToModel(player)));
	}

	public Observable<Set<Player>> getConnectedPlayers() {
		return ClientPlayerMappingResolver.getConnectedPlayers()
				.map(data -> data.values()
						.stream()
						.map(this::getPlayerById)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.collect(Collectors.toSet()));
	}

	public Optional<Player> getPlayerById(String playerId) {
		return playerDao.findById(playerId).map(PlayerService::convertEntityToModel);
	}

	public static Player convertEntityToModel(PlayerEntity entity) {
		if (entity == null) {
			return null;
		}

		return new Player(entity.getId(), entity.getNickname(), entity.getRole());
	}
}
