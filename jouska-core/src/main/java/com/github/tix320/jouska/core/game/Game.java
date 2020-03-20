package com.github.tix320.jouska.core.game;

import java.util.List;

import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.None;

public interface Game {

	void start();

	void turn(Point point);

	GameSettings getSettings();

	CellInfo[][] getBoard();

	Observable<CellChange> turns();

	List<Point> getPointsBelongedToPlayer(Player player);

	List<InGamePlayer> getPlayers();

	InGamePlayer getCurrentPlayer();

	InGamePlayer ownerOfPoint(Point point);

	Statistics getStatistics();

	Observable<InGamePlayer> lostPlayers();

	MonoObservable<InGamePlayer> winner();

	Observable<PlayerWithPoints> kickedPlayers();

	void kick(Player player);

	void forceCompleteGame(Player winner);

	MonoObservable<None> completed();

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
