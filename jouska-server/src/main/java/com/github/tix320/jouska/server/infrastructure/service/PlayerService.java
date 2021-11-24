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
import com.github.tix320.jouska.server.infrastructure.origin.AuthenticationOrigin;
import com.github.tix320.kiwi.observable.Observable;

public class PlayerService {

	private final PlayerDao playerDao;

	private final AuthenticationOrigin authenticationOrigin;

	private final ClientPlayerMappingResolver clientPlayerMappingResolver;

	public PlayerService(PlayerDao playerDao, AuthenticationOrigin authenticationOrigin,
						 ClientPlayerMappingResolver clientPlayerMappingResolver) {
		this.playerDao = playerDao;
		this.authenticationOrigin = authenticationOrigin;
		this.clientPlayerMappingResolver = clientPlayerMappingResolver;
	}

	public LoginAnswer login(long clientId, Credentials credentials) {
		Optional<PlayerEntity> playerByCredentials = playerDao.findPlayerByCredentials(credentials);
		if (playerByCredentials.isEmpty()) {
			return new LoginAnswer(LoginResult.INVALID_CREDENTIALS, null);
		}

		PlayerEntity playerEntity = playerByCredentials.get();

		Optional<Long> clientIdByPlayer = clientPlayerMappingResolver.getClientIdByPlayer(playerEntity.getId());

		if (clientIdByPlayer.isPresent()) {
			return new LoginAnswer(LoginResult.ALREADY_LOGGED, convertEntityToModel(playerEntity));
		}
		else {
			clientPlayerMappingResolver.setMapping(clientId, playerEntity.getId());

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

		Long existingClientId = clientPlayerMappingResolver.removeByPlayerId(playerId);
		if (existingClientId != null) {
			authenticationOrigin.logout(existingClientId);
			EventDispatcher.fire(new PlayerLogoutEvent(convertEntityToModel(playerEntity)));
		}

		clientPlayerMappingResolver.setMapping(clientId, playerId);
		Player player = convertEntityToModel(playerEntity);
		EventDispatcher.fire(new PlayerLoginEvent(player));
		return new LoginAnswer(LoginResult.SUCCESS, player);
	}


	public void logout(long clientId) {
		String playerId = clientPlayerMappingResolver.removeByClientId(clientId);
		if (playerId == null) {
			throw new NotAuthenticatedException(String.format("Client `%s` not authenticated yet", clientId));
		}
		PlayerEntity player = playerDao.findById(playerId).orElseThrow();
		EventDispatcher.fire(new PlayerLogoutEvent(convertEntityToModel(player)));
	}

	public Observable<Set<Player>> getConnectedPlayers() {
		return clientPlayerMappingResolver.getConnectedPlayers()
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
