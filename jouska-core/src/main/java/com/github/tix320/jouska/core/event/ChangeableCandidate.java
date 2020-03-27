package com.github.tix320.jouska.core.event;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

/**
 * @author Tigran Sargsyan on 26-Mar-20.
 */
public interface ChangeableCandidate {

	Observable<? extends Change> changes();
}
