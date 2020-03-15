package com.github.tix320.jouska.server.service.endpoint.role;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.util.None;

public class AuthorizationInterceptor implements AnnotationInterceptor<Object, Role> {

	@Override
	public Class<Role> getAnnotationClass() {
		return Role.class;
	}

	@Override
	public Object intercept(Role annotation, InterceptionContext<Object> context) {
		Player player = (Player) context.getData().get("user");
		if (player == null) {
			throw new IllegalStateException(
					String.format("Player is absent to check role for method %s", context.getMethod()));
		}

		String playerId = player.getId();
		RoleName roleName = player.getRole();
		if (roleName.compareTo(annotation.value()) < 0) {
			throw new UnauthorizedException(
					String.format("Player `%s` with role `%s` does not have access to method %s", playerId, roleName,
							context.getMethod()));
		}
		return None.SELF;
	}
}
