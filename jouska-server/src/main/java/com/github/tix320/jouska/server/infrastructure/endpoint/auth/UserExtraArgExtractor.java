package com.github.tix320.jouska.server.infrastructure.endpoint.auth;

import java.lang.reflect.Method;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.jouska.server.infrastructure.ClientPlayerMappingResolver;
import com.github.tix320.jouska.server.infrastructure.service.PlayerService;
import com.github.tix320.sonder.api.common.communication.Headers;
import com.github.tix320.sonder.api.common.rpc.extra.EndpointExtraArgExtractor;
import com.github.tix320.sonder.api.common.rpc.extra.ExtraParamDefinition;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
public class UserExtraArgExtractor implements EndpointExtraArgExtractor<CallerUser, Player> {

	@Override
	public ExtraParamDefinition<CallerUser, Player> getParamDefinition() {
		return new ExtraParamDefinition<>(CallerUser.class, Player.class, false);
	}

	@Override
	public Player extract(CallerUser annotation, Headers headers, Method method) {
		long clientId = headers.getNonNullLong(Headers.SOURCE_ID);

		String playerId = ClientPlayerMappingResolver.getPlayerIdByClientId(clientId)
				.orElseThrow(() -> new NotAuthenticatedException(
						String.format("Client with id %s not authenticated for method call %s", clientId, method)));

		Player player = PlayerService.findPlayerById(playerId).orElseThrow();

		Role role = method.getAnnotation(Role.class);

		if (role != null) {
			checkAuthorization(player, role, method);
		}

		return player;
	}

	private void checkAuthorization(Player player, Role role, Method method) {
		String playerId = player.getId();
		RoleName roleName = player.getRole();
		if (roleName.compareTo(role.value()) < 0) {
			throw new UnauthorizedException(
					String.format("Player `%s` with role `%s` does not have access to method %s", playerId, roleName,
							method));
		}
	}
}
