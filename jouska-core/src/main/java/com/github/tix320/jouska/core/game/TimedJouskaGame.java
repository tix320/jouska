package com.github.tix320.jouska.core.game;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public class TimedJouskaGame implements JouskaGame {

	private final JouskaGame jouskaGame;

	private final int turnTimeSeconds;

	private final int gameDurationMinutes;

	private final Timer turnTimer;
	private TimerTask lastTurnTimerTask;
	private TimerTask gameTimerTask;

	public static JouskaGame create(JouskaGame jouskaGame, int turnTimeSeconds, int gameDurationMinutes) {
		return COMPLETE_CHECKER_PROXY.create(
				START_CHECKER_PROXY.create(new TimedJouskaGame(jouskaGame, turnTimeSeconds, gameDurationMinutes)));
	}

	private TimedJouskaGame(JouskaGame jouskaGame, int turnTimeSeconds, int gameDurationMinutes) {
		this.jouskaGame = jouskaGame;
		this.turnTimeSeconds = turnTimeSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		this.turnTimer = new Timer(true);
		gameTimerTask = new GameTimerTask();
	}

	@Override
	public void start() {
		Lock lock = getLock();
		try {
			lock.lock();
			jouskaGame.onComplete().subscribe(players -> {
				cancelTurnTimer();
				gameTimerTask.cancel();
			});
			new Timer(true).schedule(gameTimerTask, Duration.ofMinutes(gameDurationMinutes).toMillis());

			jouskaGame.turns().subscribe(point -> runTurnTimer());

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

	@Override
	public void turn(Point point) {
		Lock lock = jouskaGame.getLock();
		try {
			lock.lock();
			cancelTurnTimer();
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
		Lock lock = jouskaGame.getLock();
		try {
			lock.lock();
			cancelTurnTimer();
			gameTimerTask.cancel();
			jouskaGame.forceCompleteGame(winner);
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public MonoObservable<List<Player>> onComplete() {
		return jouskaGame.onComplete();
	}

	@Override
	public Lock getLock() {
		return jouskaGame.getLock();
	}

	private void runTurnTimer() {
		lastTurnTimerTask = new TurnTimerTask();
		turnTimer.schedule(lastTurnTimerTask, Duration.ofSeconds(turnTimeSeconds).toMillis());
	}

	private void cancelTurnTimer() {
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
