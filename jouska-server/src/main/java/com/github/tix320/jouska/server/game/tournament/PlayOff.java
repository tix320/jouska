package com.github.tix320.jouska.server.game.tournament;

import java.util.List;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface PlayOff {

	Observable<List<List<Game>>> games();

	MonoObservable<Boolean> completed();
}
