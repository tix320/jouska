package com.github.tix320.jouska.server.game.tournament;

import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface PlayOff {

	Set<Player> getWaitingPlayers();

	Observable<List<Game>> games();
}
