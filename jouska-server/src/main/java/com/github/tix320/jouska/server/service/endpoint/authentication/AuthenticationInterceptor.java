package com.github.tix320.jouska.server.service.endpoint.authentication;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.server.service.application.NotLoggedException;
import com.github.tix320.jouska.server.service.application.PlayerService;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

public class AuthenticationInterceptor implements AnnotationInterceptor<Object, NeedAuthentication> {

	@Override
	public Class<NeedAuthentication> getAnnotationClass() {
		return NeedAuthentication.class;
	}

	@Override
	public Object intercept(NeedAuthentication annotation, InterceptionContext<Object> context) {
		Method method = context.getMethod();
		Object[] args = context.getArgs();
		int clientIdParamIndex = -1;
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			if (parameter.isAnnotationPresent(ClientID.class)) {
				clientIdParamIndex = i;
			}
		}
		if (clientIdParamIndex == -1) {
			throw new IllegalStateException(
					String.format("Method %s does not have parameter annotated with %s", method, ClientID.class));
		}

		long clientId = (long) args[clientIdParamIndex];
		try {
			Player player = PlayerService.getPlayerByClientId(clientId);
			context.putData("user", player);
			return None.SELF;
		}
		catch (NotLoggedException e) {
			throw new NotAuthenticatedException(
					String.format("Client with id %s not authenticated for method call %s", clientId, method));
		}
	}
}
