package com.github.tix320.jouska.core.application.game;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.PauseableTimer;
import com.github.tix320.jouska.core.util.SingleTaskTimer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.stock.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.stock.Stock;

import static java.util.stream.Collectors.toMap;

public final class TimedGame implements Game {

	private final Game game;

	private final int turnDurationSeconds;

	private final SingleTaskTimer turnTimer;

	private final Map<Player, PlayerTimer> playerTimers;

	private final Stock<GameChange> changes;

	private AtomicReference<TurnInfo> currentTurnInfo;

	public static TimedGame create(Game game, TimedGameSettings settings) {
		return new TimedGame(game, settings);
	}

	private TimedGame(Game game, TimedGameSettings settings) {
		this.changes = Stock.forObject();
		this.currentTurnInfo = new AtomicReference<>();
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
		game.changes().asObservable().map(gameChange -> {
			if (gameChange instanceof PlayerTurn) {
				PlayerTurn playerTurn = (PlayerTurn) gameChange;
				TurnInfo turnInfo = currentTurnInfo.get();
				return new PlayerTimedTurn(playerTurn.getCellChange(), turnInfo.getRemainingTurnMillis(),
						turnInfo.getRemainingPlayerTotalTurnMillis());
			}
			return gameChange;

		}).subscribe(changes::add);

		completed().subscribe(players -> {
			turnTimer.destroy();
			playerTimers.values().forEach(PauseableTimer::destroy);
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
		long remainingTurnMillis = turnTimer.cancel();
		long remainingPlayerTurnMillis = playerTimers.get(currentPlayer).pause();
		currentTurnInfo.set(new TurnInfo(remainingTurnMillis, remainingPlayerTurnMillis));

		CellChange cellChange = game.turn(point);
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
	public ReadOnlyStock<GameChange> changes() {
		return changes.toReadOnly();
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
	public List<InGamePlayer> getLosers() {
		return game.getLosers();
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
	public GameState getState() {
		return game.getState();
	}

	@Override
	public MonoObservable<? extends Game> completed() {
		return game.completed().peek(o -> changes.close()).toMono();
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
				if (!game.getCurrentPlayer().getRealPlayer().equals(player)) {
					return; // Player
				}

				List<Point> points = getPointsBelongedToPlayer(player);
				int randomIndex = (int) (Math.random() * points.size());
				Point randomPoint = points.get(randomIndex);
				turn(randomPoint);
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

	private final static class TurnInfo {

		private final long remainingTurnMillis;

		private final long remainingPlayerTotalTurnMillis;

		private TurnInfo(long remainingTurnMillis, long remainingPlayerTotalTurnMillis) {
			this.remainingTurnMillis = remainingTurnMillis;
			this.remainingPlayerTotalTurnMillis = remainingPlayerTotalTurnMillis;
		}

		public long getRemainingTurnMillis() {
			return remainingTurnMillis;
		}

		public long getRemainingPlayerTotalTurnMillis() {
			return remainingPlayerTotalTurnMillis;
		}
	}

}
