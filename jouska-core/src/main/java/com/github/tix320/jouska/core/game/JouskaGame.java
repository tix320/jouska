package com.github.tix320.jouska.core.game;

import java.util.List;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.infastructure.CheckCompleted;
import com.github.tix320.jouska.core.infastructure.CheckStarted;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.proxy.AnnotationProxyCreator;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface JouskaGame {

	@CheckCompleted
	void start();

	@CheckStarted
	@CheckCompleted
	void turn(Point point);

	@CheckStarted
	CellInfo[][] getBoard();

	Observable<CellChange> turns();

	@CheckStarted
	List<Point> getPointsBelongedToPlayer(Player player);

	@CheckCompleted
	Player getCurrentPlayer();

	@CheckStarted
	Player ownerOfPoint(Point point);

	Statistics getStatistics();

	Observable<Player> lostPlayers();

	Observable<PlayerWithPoints> kickedPlayers();

	@CheckStarted
	void kick(Player player);

	@CheckStarted
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

	AnnotationProxyCreator<JouskaGame, CheckStarted> START_CHECKER_PROXY = new AnnotationProxyCreator<>(
			JouskaGame.class, CheckStarted.class, (annotationOptional, target) -> {
		if (annotationOptional.isPresent()) {
			if (!target.isStarted()) {
				throw new IllegalStateException("Game does not started");
			}
		}
	});

	AnnotationProxyCreator<JouskaGame, CheckStarted> COMPLETE_CHECKER_PROXY = new AnnotationProxyCreator<>(
			JouskaGame.class, CheckCompleted.class, (annotationOptional, target) -> {
		if (annotationOptional.isPresent()) {
			if (target.isCompleted()) {
				throw new IllegalStateException("Game already completed");
			}
		}
	});
}
