package com.github.tix320.jouska.core.application.tournament;

import java.util.List;

import com.github.tix320.jouska.core.event.ChangeableCandidate;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public interface Tournament extends ChangeableCandidate {

	List<Player> getPlayers();

	List<Group> getGroups();

	ReadOnlyProperty<PlayOff> playOff();

	@Override
	Observable<TournamentChange> changes();
}
