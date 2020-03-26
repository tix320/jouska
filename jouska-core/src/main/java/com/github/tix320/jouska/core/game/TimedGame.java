package com.github.tix320.jouska.core.game;

import java.time.Duration;
import java.util.*;
import java.util.Map.Entry;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.None;

public class TimedGame implements Game {

	private final Game game;

	private final int turnDurationSeconds;

	private final int gameDurationMinutes;

	private final Timer turnTimer;
	private final Timer gameTimer;
	private volatile TurnTimerTask lastTurnTimerTask;
	private volatile GameTimerTask gameTimerTask;

	public static TimedGame create(Game game, int turnTimeSeconds, int gameDurationMinutes) {
		return new TimedGame(game, turnTimeSeconds, gameDurationMinutes);
	}

	private TimedGame(Game game, int turnDurationSeconds, int gameDurationMinutes) {
		this.game = game;
		this.turnDurationSeconds = turnDurationSeconds;
		this.gameDurationMinutes = gameDurationMinutes;
		this.turnTimer = new Timer(true);
		this.gameTimer = new Timer(true);
		this.gameTimerTask = new GameTimerTask();
		this.lastTurnTimerTask = new TurnTimerTask(game.getCurrentPlayer().getRealPlayer());
	}

	@Override
	public void start() {
		completed().subscribe(players -> {
			lastTurnTimerTask.cancel();
			gameTimerTask.cancel();
		});

		game.start();

		turnTimer.schedule(lastTurnTimerTask, Duration.ofSeconds(turnDurationSeconds).toMillis());
		gameTimer.schedule(gameTimerTask, Duration.ofMinutes(gameDurationMinutes).toMillis());
	}

	@Override
	public boolean isStarted() {
		return game.isStarted();
	}

	@Override
	public synchronized CellChange turn(Point point) {
		lastTurnTimerTask.cancel();
		CellChange cellChange = game.turn(point);
		double seconds = calculateApproximateTime(cellChange);
		Player currentPLayer = getCurrentPlayer().getRealPlayer();
		int nextTurnSeconds = ((int) Math.ceil(seconds)) + turnDurationSeconds + 1; // non-negotiable.
		runTurnTimer(currentPLayer, nextTurnSeconds);
		return cellChange;
	}

	@Override
	public BoardCell[][] getBoard() {
		return game.getBoard();
	}

	@Override
	public Observable<GameChange> changes() {
		return game.changes();
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
	public List<InGamePlayer> getActivePlayers() {
		return game.getActivePlayers();
	}

	@Override
	public InGamePlayer getCurrentPlayer() {
		return game.getCurrentPlayer();
	}

	@Override
	public Optional<InGamePlayer> ownerOfPoint(Point point) {
		return game.ownerOfPoint(point);
	}

	@Override
	public Statistics getStatistics() {
		return game.getStatistics();
	}

	@Override
	public List<InGamePlayer> getLostPlayers() {
		return game.getLostPlayers();
	}

	@Override
	public Optional<InGamePlayer> getWinner() {
		return game.getWinner();
	}

	@Override
	public List<PlayerWithPoints> getKickedPlayers() {
		return game.getKickedPlayers();
	}

	@Override
	public synchronized PlayerWithPoints kick(Player player) {
		return game.kick(player);
	}

	@Override
	public synchronized void forceCompleteGame(Player winner) {
		game.forceCompleteGame(winner);
	}

	@Override
	public MonoObservable<None> completed() {
		return game.completed();
	}

	@Override
	public boolean isCompleted() {
		return game.isCompleted();
	}

	private void runTurnTimer(Player player, int seconds) {
		lastTurnTimerTask.cancel();
		lastTurnTimerTask = new TurnTimerTask(player);
		turnTimer.schedule(lastTurnTimerTask, Duration.ofSeconds(seconds).toMillis());
	}

	private static double calculateApproximateTime(CellChange root) {
		double tileAnimationSeconds = Constants.GAME_BOARD_TILE_ANIMATION_SECONDS;

		double animationTime = tileAnimationSeconds;

		if (root.getChildren().isEmpty()) {
			return animationTime;
		}

		if (root.getChildren().size() != 1) {
			throw new IllegalStateException("Illegal size: " + root.getChildren().size());
		}

		Deque<CellChange> changesStack = new LinkedList<>();
		changesStack.add(root);
		while (!changesStack.isEmpty()) {
			CellChange cellChange = changesStack.removeFirst();

			if (cellChange.getChildren().isEmpty()) {
				continue;
			}

			if (cellChange.isCollapse()) {
				if (cellChange.getChildren().size() != 1) {
					throw new IllegalStateException("Illegal size: " + cellChange.getChildren().size());
				}

				CellChange collapsingChange = cellChange.getChildren().get(0);
				changesStack.addAll(collapsingChange.getChildren());
				animationTime += tileAnimationSeconds;
			}

			animationTime += tileAnimationSeconds;
		}

		return animationTime;
	}

	private class TurnTimerTask extends TimerTask {

		private final Player player;

		private TurnTimerTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			synchronized (TimedGame.this) {
				List<Point> points = getPointsBelongedToPlayer(player);
				int randomIndex = (int) (Math.random() * points.size());
				Point randomPoint = points.get(randomIndex);
				try {
					turn(randomPoint);
				}
				catch (IllegalTurnActorException e) { // Real player already made their turn, skip our random turn
					System.err.println(
							"Do not pay attention to this error. If you do not have any bugs, then this is normal: " + e
									.getMessage());
				}
			}
		}
	}

	private class GameTimerTask extends TimerTask {

		@Override
		public void run() {
			synchronized (TimedGame.this) {
				Map<InGamePlayer, Integer> summaryPoints = getStatistics().summaryPoints();
				Iterator<Entry<InGamePlayer, Integer>> iterator = summaryPoints.entrySet().iterator();

				Map.Entry<InGamePlayer, Integer> maxEntry = iterator.next();
				while (iterator.hasNext()) {
					Entry<InGamePlayer, Integer> entry = iterator.next();
					if (entry.getValue() > maxEntry.getValue()) {
						maxEntry = entry;
					}
				}
				InGamePlayer winner = maxEntry.getKey();
				forceCompleteGame(winner.getRealPlayer());
			}
		}
	}
}
