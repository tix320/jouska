package com.github.tix320.jouska.core.game;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public interface Game {

	void start();

	void turn(Point point);

	GameSettings getSettings();

	CellInfo[][] getBoard();

	ReadOnlyStock<CellChange> turns();

	List<Point> getPointsBelongedToPlayer(Player player);

	List<InGamePlayer> getPlayers();

	InGamePlayer getCurrentPlayer();

	InGamePlayer ownerOfPoint(Point point);

	Statistics getStatistics();

	ReadOnlyStock<InGamePlayer> lostPlayers();

	ReadOnlyStock<PlayerWithPoints> kickedPlayers();

	void kick(Player player);

	void forceCompleteGame(Player winner);

	MonoObservable<List<InGamePlayer>> onComplete();

	Lock getLock();

	boolean isStarted();

	boolean isCompleted();

	class CellChange {
		public final Point point;
		public final CellInfo cellInfo;
		public final boolean collapse;
		public final List<CellChange> children;

		public CellChange(Point point, CellInfo cellInfo, boolean collapse, List<CellChange> children) {
			this.point = point;
			this.cellInfo = cellInfo;
			this.collapse = collapse;
			this.children = children;
		}
	}

	class PlayerWithPoints {
		public final InGamePlayer player;
		public final List<Point> points;

		public PlayerWithPoints(InGamePlayer player, List<Point> points) {
			this.player = player;
			this.points = points;
		}
	}
}
