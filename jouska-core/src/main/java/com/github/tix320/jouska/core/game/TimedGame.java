package com.github.tix320.jouska.core.game;

import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.github.tix320.jouska.core.game.proxy.CompletedInterceptor;
import com.github.tix320.jouska.core.game.proxy.StartedInterceptor;
import com.github.tix320.jouska.core.game.proxy.ThrowIfCompleted;
import com.github.tix320.jouska.core.game.proxy.ThrowIfNotStarted;
import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.proxy.AnnotationBasedProxyCreator;
import com.github.tix320.kiwi.api.proxy.ProxyCreator;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.SimplePublisher;
import com.github.tix320.kiwi.api.util.None;

public class TimedGame implements Game {

	private static final ProxyCreator<TimedGame> PROXY = new AnnotationBasedProxyCreator<>(TimedGame.class,
			List.of(new StartedInterceptor(), new CompletedInterceptor()));

	private final Game game;

	private final int turnDurationSeconds;

	private final int gameDurationMinutes;

	private final Timer turnTimer;
	private volatile TimerTask lastTurnTimerTask;
	private volatile TimerTask gameTimerTask;

	private SimplePublisher<None> turnTimeExpirationPublisher;
	private MonoPublisher<None> gameTimeExpirationPublisher;

	public static TimedGame create(Game game, int turnTimeSeconds, int gameDurationMinutes) {
		return PROXY.create(game, turnTimeSeconds, gameDurationMinutes);
	}

	public TimedGame(Game game, int turnDurationSeconds, int gameDurationMinutes) {
		this.game = game;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		this.turnTimer = new Timer(true);
		gameTimerTask = new GameTimerTask();
		this.turnTimeExpirationPublisher = Publisher.simple();
		this.gameTimeExpirationPublisher = Publisher.mono();
	}

	@ThrowIfCompleted
	@Override
	public void start() {
		completed().subscribe(players -> {
			cancelTurnTimerTask();
			gameTimerTask.cancel();
			turnTimeExpirationPublisher.complete();
		});
		new Timer(true).schedule(gameTimerTask, Duration.ofMinutes(gameDurationMinutes).toMillis());

		game.start();
	}

	@Override
	public boolean isStarted() {
		return game.isStarted();
	}

	@ThrowIfNotStarted
	@ThrowIfCompleted
	@Override
	public void turn(Point point) {
		cancelTurnTimerTask();
		game.turn(point);
	}

	@Override
	public GameSettings getSettings() {
		GameSettings settings = game.getSettings();
		return new GameSettings(settings.getName(), GameType.TIMED, settings.getBoardType(), settings.getPlayersCount(),
				turnDurationSeconds, gameDurationMinutes);
	}

	@Override
	public CellInfo[][] getBoard() {
		return game.getBoard();
	}

	@Override
	public Observable<CellChange> turns() {
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
	public Observable<InGamePlayer> lostPlayers() {
		return game.lostPlayers();
	}

	@Override
	public MonoObservable<InGamePlayer> winner() {
		return game.winner();
	}

	@Override
	public Observable<PlayerWithPoints> kickedPlayers() {
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
	public MonoObservable<None> completed() {
		return game.completed();
	}

	public Observable<None> turnTimeExpiration() {
		return turnTimeExpirationPublisher.asObservable();
	}

	public MonoObservable<None> gameTimeExpiration() {
		return gameTimeExpirationPublisher.asObservable();
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
			turnTimeExpirationPublisher.publish(None.SELF);
		}
	}

	private class GameTimerTask extends TimerTask {

		@Override
		public void run() {
			gameTimeExpirationPublisher.publish(None.SELF);
		}
	}
}
