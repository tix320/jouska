package com.github.tix320.jouska.core.application.tournament;

import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.application.game.creation.TournamentSettings;
import com.github.tix320.jouska.core.infrastructure.concurrent.LockOwner;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

/**
 * NOTE: implementations must be thread-safe.
 */
public interface Tournament extends LockOwner {

	/**
	 * Get tournament settings.
	 *
	 * @return settings.
	 */
	TournamentSettings getSettings();

	/**
	 * Add player to tournament.
	 *
	 * @throws TournamentIllegalStateException if already started
	 * @throws TournamentAlreadyFullException  if no space already available.
	 */
	boolean addPlayer(Player player);

	/**
	 * Remove player from tournament.
	 *
	 * @return true if removed.
	 *
	 * @throws TournamentIllegalStateException if already started
	 */
	boolean removePlayer(Player player);

	/**
	 * Start the tournament.
	 *
	 * @throws TournamentIllegalStateException if already started
	 * @see #addPlayer(Player)
	 */
	void start();

	/**
	 * Get players
	 */
	Set<Player> getPlayers();

	/**
	 * Get groups.
	 *
	 * @see Group
	 */
	List<? extends Group> getGroups();

	/**
	 * Get play-off property
	 *
	 * @see PlayOff
	 */
	ReadOnlyProperty<? extends PlayOff> playOff();

	/**
	 * Get tournament state.
	 */
	TournamentState getState();

	/**
	 * Get observable to subscribe game completeness.
	 */
	MonoObservable<? extends Tournament> completed();
}
