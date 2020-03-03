package com.github.tix320.jouska.core.game;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface JouskaGame {

	void start();

	void turn(Point point);

	CellInfo[][] getBoard();

	Observable<CellChange> turns();

	List<Point> getPointsBelongedToPlayer(Player player);

	Player getCurrentPlayer();

	Player ownerOfPoint(Point point);

	Statistics getStatistics();

	Observable<Player> lostPlayers();

	Observable<PlayerWithPoints> kickedPlayers();

	void kick(Player player);

	void forceCompleteGame(Player winner);

	MonoObservable<List<Player>> onComplete();

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
		public final Player player;
		public final List<Point> points;

		public PlayerWithPoints(Player player, List<Point> points) {
			this.player = player;
			this.points = points;
		}
	}
}
