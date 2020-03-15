package com.github.tix320.jouska.server.game.tournament;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public interface Tournament {

	List<Group> getGroups();

	ReadOnlyProperty<PlayOff> playOff();
}
