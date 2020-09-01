package com.github.tix320.jouska.server.infrastructure.endpoint.auth;

import java.lang.reflect.Method;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Role;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.sonder.api.common.communication.Headers;
import com.github.tix320.sonder.api.common.rpc.extra.EndpointExtraArgInjector;
import com.github.tix320.sonder.api.common.rpc.extra.ExtraParamDefinition;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
public class UserExtraArgInjector implements EndpointExtraArgInjector<CallerUser, Player> {

	private final PlayerService playerService;

	private final ClientPlayerMappingResolver clientPlayerMappingResolver;

	public UserExtraArgInjector(PlayerService playerService, ClientPlayerMappingResolver clientPlayerMappingResolver) {
		this.playerService = playerService;
		this.clientPlayerMappingResolver = clientPlayerMappingResolver;
	}

	@Override
	public ExtraParamDefinition<CallerUser, Player> getParamDefinition() {
		return new ExtraParamDefinition<>(CallerUser.class, Player.class, false);
	}

	@Override
	public Player extract(Method method, CallerUser annotation, Headers headers) {
		long clientId = headers.getNonNullLong(Headers.SOURCE_ID);

		String playerId = clientPlayerMappingResolver.getPlayerIdByClientId(clientId)
				.orElseThrow(() -> new NotAuthenticatedException(
						String.format("Client with id %s not authenticated for method call %s", clientId, method)));

		Player player = playerService.getPlayerById(playerId).orElseThrow();

		Role role = annotation.role();

		checkAuthorization(player, role, method);

		return player;
	}

	private void checkAuthorization(Player player, Role role, Method method) {
		String playerId = player.getId();
		Role playerRole = player.getRole();
		if (playerRole.compareTo(role) < 0) {
			throw new UnauthorizedException(
					String.format("Player `%s` with role `%s` does not have access to method %s", playerId, playerRole,
							method));
		}
	}
}
