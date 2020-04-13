package com.github.tix320.jouska.core.application.game;

import java.util.List;
import java.util.Optional;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public interface Game {

	void start();

	CellChange turn(Point point);

	BoardCell[][] getBoard();

	ReadOnlyStock<GameChange> changes();

	List<Point> getPointsBelongedToPlayer(Player player);

	List<InGamePlayer> getPlayers();

	List<InGamePlayer> getActivePlayers();

	InGamePlayer getCurrentPlayer();

	Optional<InGamePlayer> ownerOfPoint(Point point);

	Statistics getStatistics();

	List<InGamePlayer> getLosers();

	Optional<InGamePlayer> getWinner();

	List<PlayerWithPoints> getKickedPlayers();

	PlayerWithPoints kick(Player player);

	void forceCompleteGame(Player winner);

	GameState getState();

	boolean isStarted();

	boolean isCompleted();

	MonoObservable<? extends Game> completed();
}
