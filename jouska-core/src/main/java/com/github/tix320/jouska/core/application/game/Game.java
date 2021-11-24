package com.github.tix320.jouska.core.application.game;

import java.util.List;
import java.util.Optional;

import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.infrastructure.concurrent.LockOwner;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.property.Stock;

/**
 * NOTE: Implementations must be thread-safe.
 */
public interface Game extends LockOwner {

	/**
	 * Get game settings.
	 *
	 * @return settings.
	 */
	GameSettings getSettings();

	/**
	 * Add player to game.
	 *
	 * @throws GameIllegalStateException if already started
	 * @throws IllegalArgumentException  if player already added.
	 * @throws GameAlreadyFullException  if no space already available.
	 */
	void addPlayer(GamePlayer player);

	/**
	 * Remove player from game.
	 *
	 * @return true if removed.
	 * @throws GameIllegalStateException if already started
	 */
	boolean removePlayer(Player player);

	/**
	 * Shuffle players before start.
	 * Also colors will be shuffled.
	 *
	 * @throws GameIllegalStateException if already started
	 */
	void shufflePLayers();

	/**
	 * Start the game.
	 *
	 * @throws GameIllegalStateException if already started
	 * @throws GameIllegalStateException if players not full.
	 * @see #addPlayer(GamePlayer)
	 */
	void start();

	/**
	 * Get current board copy, read only.
	 *
	 * @return board.
	 */
	ReadOnlyGameBoard getBoard();

	/**
	 * @throws GameIllegalStateException if not started or already completed
	 * @throws IllegalTurnActorException if point do not belong to current player
	 */
	CellChange turn(Point point);

	/**
	 * @throws GameIllegalStateException if not started
	 * @throws IllegalArgumentException  if given player not participant of this game
	 */
	List<Point> getPointsBelongedToPlayer(Player player);

	/**
	 * Get players
	 */
	List<Player> getPlayers();

	/**
	 * Get players with their colors.
	 */
	List<GamePlayer> getGamePlayers();

	/**
	 * Get active players, i.e. who currently playing.
	 */
	List<GamePlayer> getActivePlayers();

	/**
	 * @throws GameIllegalStateException if already completed
	 */
	GamePlayer getCurrentPlayer();

	/**
	 * @throws GameIllegalStateException if not started
	 */
	Optional<GamePlayer> ownerOfPoint(Point point);

	/**
	 * Get game statistics.
	 */
	Statistics getStatistics();

	/**
	 * Get losers.
	 */
	List<GamePlayer> getLosers();

	/**
	 * Get game winner, present if game completed.
	 */
	Optional<GamePlayer> getWinner();

	/**
	 * Get players, who forcibly kicked from game
	 */
	List<PlayerWithPoints> getKickedPlayers();

	/**
	 * @throws GameIllegalStateException if not started or already completed
	 */
	PlayerWithPoints kick(Player player);

	/**
	 * @throws GameIllegalStateException if not started or already completed
	 */
	void forceCompleteGame(Player winner);

	/**
	 * Get current game state.
	 */
	GameState getState();

	/**
	 * @return true if game is started.
	 */
	boolean isStarted();

	/**
	 * @return true if game is completed.
	 */
	boolean isCompleted();

	/**
	 * Get observable to subscribe game completeness.
	 */
	MonoObservable<? extends Game> completed();

	/**
	 * Get stock of game changes.
	 */
	Stock<GameChange> changes();
}
