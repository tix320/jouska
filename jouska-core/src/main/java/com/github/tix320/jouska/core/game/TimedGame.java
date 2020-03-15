package com.github.tix320.jouska.core.game;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.core.game.proxy.CompletedInterceptor;
import com.github.tix320.jouska.core.game.proxy.StartedInterceptor;
import com.github.tix320.jouska.core.game.proxy.ThrowIfCompleted;
import com.github.tix320.jouska.core.game.proxy.ThrowIfNotStarted;
import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.proxy.AnnotationBasedProxyCreator;
import com.github.tix320.kiwi.api.proxy.ProxyCreator;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;

public class TimedGame implements Game {

	private static final ProxyCreator<TimedGame> PROXY = new AnnotationBasedProxyCreator<>(TimedGame.class,
			List.of(new StartedInterceptor(), new CompletedInterceptor()));

	private final Game game;

	private final int turnDurationSeconds;

	private final int gameDurationMinutes;

	private final Timer turnTimer;
	private TimerTask lastTurnTimerTask;
	private TimerTask gameTimerTask;

	public static TimedGame create(Game game, int turnTimeSeconds, int gameDurationMinutes) {
		return PROXY.create(game, turnTimeSeconds, gameDurationMinutes);
	}

	public TimedGame(Game game, int turnDurationSeconds, int gameDurationMinutes) {
		this.game = game;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		this.turnTimer = new Timer(true);
		gameTimerTask = new GameTimerTask();
	}

	@ThrowIfCompleted
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

			game.start();
			runTurnTimer();

		}
		finally {
			lock.unlock();
		}

	}

	@Override
	public boolean isStarted() {
		return game.isStarted();
	}

	@ThrowIfNotStarted
	@ThrowIfCompleted
	@Override
	public void turn(Point point) {
		Lock lock = game.getLock();
		try {
			lock.lock();
			cancelTurnTimerTask();
			game.turn(point);
		}
		finally {
			lock.unlock();
		}
	}

	@Override
	public GameSettings getSettings() {
		GameSettings settings = game.getSettings();
		return new GameSettings(settings.getName(), GameType.TIMED, settings.getBoardType(),
				settings.getPlayersCount(), turnDurationSeconds, gameDurationMinutes);
	}

	@Override
	public CellInfo[][] getBoard() {
		return game.getBoard();
	}

	@Override
	public ReadOnlyStock<CellChange> turns() {
		return game.turns();
	}

	@Override
	public List<Point> getPointsBelongedToPlayer(Player player) {
		return game.getPointsBelongedToPlayer(player);
	}

	@Override
	public List<InGamePlayer> getPlayers() {
		return game.getPlayers();
	}

	@Override
	public InGamePlayer getCurrentPlayer() {
		return game.getCurrentPlayer();
	}

	@Override
	public InGamePlayer ownerOfPoint(Point point) {
		return game.ownerOfPoint(point);
	}

	@Override
	public Statistics getStatistics() {
		return game.getStatistics();
	}

	@Override
	public ReadOnlyStock<InGamePlayer> lostPlayers() {
		return game.lostPlayers();
	}

	@Override
	public ReadOnlyStock<PlayerWithPoints> kickedPlayers() {
		return game.kickedPlayers();
	}

	@Override
	public void kick(Player player) {
		game.kick(player);
	}

	@Override
	public void forceCompleteGame(Player winner) {
		game.forceCompleteGame(winner);
	}

	@Override
	public MonoObservable<List<InGamePlayer>> onComplete() {
		return game.onComplete();
	}

	@Override
	public Lock getLock() {
		return game.getLock();
	}

	public void runTurnTimer() {
		lastTurnTimerTask = new TurnTimerTask();
		turnTimer.schedule(lastTurnTimerTask, Duration.ofSeconds(turnDurationSeconds).toMillis());
	}

	private void cancelTurnTimerTask() {
		lastTurnTimerTask.cancel();
	}

	@Override
	public boolean isCompleted() {
		return game.isCompleted();
	}

	private class TurnTimerTask extends TimerTask {

		@Override
		public void run() {
			Lock lock = getLock();
			try {
				if (lock.tryLock()) {
					InGamePlayer currentPlayer = getCurrentPlayer();
					List<Point> points = getPointsBelongedToPlayer(currentPlayer.getPlayer());
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
				Map<InGamePlayer, Integer> summaryPoints = getStatistics().summaryPoints().get();
				Iterator<Entry<InGamePlayer, Integer>> iterator = summaryPoints.entrySet().iterator();

				Map.Entry<InGamePlayer, Integer> maxEntry = iterator.next();
				while (iterator.hasNext()) {
					Entry<InGamePlayer, Integer> entry = iterator.next();
					if (entry.getValue() > maxEntry.getValue()) {
						maxEntry = entry;
					}
				}
				InGamePlayer winner = maxEntry.getKey();
				forceCompleteGame(winner.getPlayer());
			}
			finally {
				lock.unlock();
			}
		}
	}
}
