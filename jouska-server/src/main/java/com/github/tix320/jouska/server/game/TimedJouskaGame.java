package com.github.tix320.jouska.server.game;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.game.Statistics;
import com.github.tix320.jouska.core.infastructure.CheckCompleted;
import com.github.tix320.jouska.core.infastructure.CheckStarted;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.proxy.AnnotationBasedProxyCreator;
import com.github.tix320.kiwi.api.proxy.AnnotationInterceptor;
import com.github.tix320.kiwi.api.proxy.ProxyCreator;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.None;

public class TimedJouskaGame implements JouskaGame {

	private static final ProxyCreator<TimedJouskaGame> PROXY = new AnnotationBasedProxyCreator<>(TimedJouskaGame.class,
			List.of(new AnnotationInterceptor<>(CheckStarted.class, (method, target) -> {
				if (!target.isStarted()) {
					throw new IllegalStateException("Game does not started");
				}
				return None.SELF;
			}), new AnnotationInterceptor<>(CheckCompleted.class, (method, target) -> {
				if (target.isCompleted()) {
					throw new IllegalStateException("Game already completed");
				}
				return None.SELF;
			})));

	private final JouskaGame jouskaGame;

	private final int turnTimeSeconds;

	private final int gameDurationMinutes;

	private final Timer turnTimer;
	private TimerTask lastTurnTimerTask;
	private TimerTask gameTimerTask;

	public static TimedJouskaGame create(JouskaGame jouskaGame, int turnTimeSeconds, int gameDurationMinutes) {
		return PROXY.create(jouskaGame, turnTimeSeconds, gameDurationMinutes);
	}

	public TimedJouskaGame(JouskaGame jouskaGame, int turnTimeSeconds, int gameDurationMinutes) {
		this.jouskaGame = jouskaGame;
		this.turnTimeSeconds = turnTimeSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		this.turnTimer = new Timer(true);
		gameTimerTask = new GameTimerTask();
	}

	@CheckCompleted
	@Override
	public void start() {
		Lock lock = getLock();
		try {
			lock.lock();
			onComplete().subscribe(players -> {
				cancelTurnTimerTask();
				gameTimerTask.cancel();
			});
			new Timer(true).schedule(gameTimerTask, Duration.ofMinutes(gameDurationMinutes).toMillis());

			jouskaGame.start();
			runTurnTimer();

		}
		finally {
			lock.unlock();
		}

	}

	@Override
	public boolean isStarted() {
		return jouskaGame.isStarted();
	}

	@CheckStarted
	@CheckCompleted
	@Override
	public void turn(Point point) {
		Lock lock = jouskaGame.getLock();
		try {
			lock.lock();
			cancelTurnTimerTask();
			jouskaGame.turn(point);
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public CellInfo[][] getBoard() {
		return jouskaGame.getBoard();
	}

	@Override
	public Observable<CellChange> turns() {
		return jouskaGame.turns();
	}

	@Override
	public List<Point> getPointsBelongedToPlayer(Player player) {
		return jouskaGame.getPointsBelongedToPlayer(player);
	}

	@Override
	public Player getCurrentPlayer() {
		return jouskaGame.getCurrentPlayer();
	}

	@Override
	public Player ownerOfPoint(Point point) {
		return jouskaGame.ownerOfPoint(point);
	}

	@Override
	public Statistics getStatistics() {
		return jouskaGame.getStatistics();
	}

	@Override
	public Observable<Player> lostPlayers() {
		return jouskaGame.lostPlayers();
	}

	@Override
	public Observable<PlayerWithPoints> kickedPlayers() {
		return jouskaGame.kickedPlayers();
	}

	@Override
	public void kick(Player player) {
		jouskaGame.kick(player);
	}

	@Override
	public void forceCompleteGame(Player winner) {
		jouskaGame.forceCompleteGame(winner);
	}

	@Override
	public MonoObservable<List<Player>> onComplete() {
		return jouskaGame.onComplete();
	}

	@Override
	public Lock getLock() {
		return jouskaGame.getLock();
	}

	public void runTurnTimer() {
		lastTurnTimerTask = new TurnTimerTask();
		turnTimer.schedule(lastTurnTimerTask, Duration.ofSeconds(turnTimeSeconds).toMillis());
	}

	private void cancelTurnTimerTask() {
		lastTurnTimerTask.cancel();
	}

	@Override
	public boolean isCompleted() {
		return jouskaGame.isCompleted();
	}

	private class TurnTimerTask extends TimerTask {

		@Override
		public void run() {
			Lock lock = getLock();
			try {
				if (lock.tryLock()) {
					Player currentPlayer = getCurrentPlayer();
					List<Point> points = getPointsBelongedToPlayer(currentPlayer);
					int randomIndex = (int) (Math.random() * points.size());
					Point randomPoint = points.get(randomIndex);
					turn(randomPoint);
				}
			}
			finally {
				lock.unlock();
			}
		}
	}

	private class GameTimerTask extends TimerTask {

		@Override
		public void run() {
			Lock lock = getLock();
			try {
				lock.lock();
				Map<Player, Integer> summaryPoints = getStatistics().summaryPoints().get();
				Iterator<Entry<Player, Integer>> iterator = summaryPoints.entrySet().iterator();

				Map.Entry<Player, Integer> maxEntry = iterator.next();
				while (iterator.hasNext()) {
					Entry<Player, Integer> entry = iterator.next();
					if (entry.getValue() > maxEntry.getValue()) {
						maxEntry = entry;
					}
				}
				Player winner = maxEntry.getKey();
				forceCompleteGame(winner);
			}
			finally {
				lock.unlock();
			}
		}
	}
}
