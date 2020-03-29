package com.github.tix320.jouska.core.application.game;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.PauseableTimer;
import com.github.tix320.jouska.core.util.SingleTaskTimer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.stock.Stock;
import com.github.tix320.kiwi.api.util.None;

import static java.util.stream.Collectors.toMap;

public final class TimedGame implements Game {

	private final Game game;

	private final int turnDurationSeconds;

	private final SingleTaskTimer turnTimer;

	private final Map<Player, PlayerTimer> playerTimers;

	private final Stock<TimedGameChange> changes;

	public static TimedGame create(Game game, TimedGameSettings settings) {
		return new TimedGame(game, settings);
	}

	private TimedGame(Game game, TimedGameSettings settings) {
		this.changes = Stock.forObject();
		this.game = game;
		this.turnDurationSeconds = settings.getTurnDurationSeconds();
		this.turnTimer = new SingleTaskTimer();
		this.playerTimers = game.getPlayers()
				.stream()
				.map(InGamePlayer::getRealPlayer)
				.collect(toMap(player -> player, player -> new PlayerTimer(player,
						TimeUnit.SECONDS.toMillis(settings.getPlayerTurnTotalDurationSeconds()))));
	}

	@Override
	public synchronized void start() {
		completed().subscribe(players -> {
			turnTimer.cancel();
			playerTimers.values().forEach(PauseableTimer::pause);
		});

		game.start();

		Player firstPlayer = game.getCurrentPlayer().getRealPlayer();

		runTurnTimer(firstPlayer, turnDurationSeconds);
	}

	@Override
	public boolean isStarted() {
		return game.isStarted();
	}

	@Override
	public synchronized CellChange turn(Point point) {
		Player currentPlayer = game.getCurrentPlayer().getRealPlayer();
		CellChange cellChange = game.turn(point);
		turnTimer.cancel();
		playerTimers.get(currentPlayer).pause();
		double seconds = calculateApproximateAnimationTime(cellChange);
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
		return Observable.concat(game.changes(), changes.asObservable());
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
	public PlayerWithPoints kick(Player player) {
		return game.kick(player);
	}

	@Override
	public void forceCompleteGame(Player winner) {
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
		turnTimer.schedule(new TurnTimerTask(player), Duration.ofSeconds(seconds).toMillis());
		playerTimers.get(player).resume();
	}

	private static double calculateApproximateAnimationTime(CellChange root) {
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

	private final class TurnTimerTask implements Runnable {

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

	private final class PlayerTimer extends PauseableTimer {

		private PlayerTimer(Player player, long delaySeconds) {
			super(delaySeconds, () -> {
				synchronized (TimedGame.this) {
					kick(player);
				}
			});
		}
	}

}
