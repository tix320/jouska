package com.github.tix320.jouska.core.application.tournament;

import java.util.List;
import java.util.Optional;

import com.github.tix320.jouska.core.application.game.creation.PlayOffSettings;
import com.github.tix320.jouska.core.infrastructure.concurrent.ThreadSafe;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.skimp.api.object.None;

public interface PlayOff extends ThreadSafe {

	PlayOffSettings getSettings();

	/**
	 * Start the play-off.
	 *
	 * @throws TournamentIllegalStateException if already started
	 */
	void start();

	List<Player> getPlayers();

	List<List<PlayOffGame>> getTours();

	MonoObservable<None> completed();

	Optional<Player> getWinner();

	Observable<None> changes();
}
