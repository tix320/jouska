package com.github.tix320.jouska.server.infrastructure.endpoint.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tix320.jouska.core.model.Role;
import com.github.tix320.sonder.api.common.rpc.extra.ExtraParamQualifier;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@ExtraParamQualifier
public @interface CallerUser {

	Role role() default Role.PLAYER;
}
