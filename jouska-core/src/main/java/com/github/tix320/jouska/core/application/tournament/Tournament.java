package com.github.tix320.jouska.core.application.tournament;

import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;

public interface Tournament {

	Set<Player> getPlayers();

	List<Group> getGroups();

	MonoObservable<PlayOff> playOff();
}
