package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.util.None;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public interface NotificationController<T> extends Controller<T> {

	MonoObservable<?> resolved();
}
