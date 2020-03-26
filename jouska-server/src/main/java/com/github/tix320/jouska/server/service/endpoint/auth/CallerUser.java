package com.github.tix320.jouska.server.service.endpoint.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.tix320.sonder.internal.common.rpc.extra.ExtraParamQualifier;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@ExtraParamQualifier
public @interface CallerUser {}
