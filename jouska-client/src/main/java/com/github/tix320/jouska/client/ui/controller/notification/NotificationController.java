package com.github.tix320.jouska.client.ui.controller.notification;

import com.github.tix320.jouska.client.infrastructure.notifcation.NotificationEvent;
import com.github.tix320.jouska.client.ui.controller.Controller;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
public interface NotificationController<T extends NotificationEvent<?,?>> extends Controller<T> {

}
