package com.github.tix320.jouska.core.application.tournament;

import java.util.List;
import java.util.Optional;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.creation.GroupSettings;
import com.github.tix320.jouska.core.application.tournament.ClassicGroup.GroupPlayer;
import com.github.tix320.jouska.core.infrastructure.concurrent.ThreadSafe;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.observable.MonoObservable;

public interface Group extends ThreadSafe {

	GroupSettings getSettings();

	/**
	 * Start the group.
	 *
	 * @throws TournamentIllegalStateException if already started
	 */
	void start();

	List<GroupPlayer> getPlayers();

	Optional<List<Player>> getWinners();

	List<Game> getGames();

	MonoObservable<? extends Group> completed();
}
