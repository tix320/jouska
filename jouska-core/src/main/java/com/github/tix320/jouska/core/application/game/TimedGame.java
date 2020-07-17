package com.github.tix320.jouska.core.application.game;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.infrastructure.UnsupportedChangeException;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.PauseableTimer;
import com.github.tix320.jouska.core.util.SingleTaskTimer;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyStock;
import com.github.tix320.kiwi.api.reactive.property.Stock;

public final class TimedGame implements RestorableGame {

	private final Game game;

	private final TimedGameSettings settings;

	private final int turnDurationSeconds;

	private final SingleTaskTimer turnTimer;

	private final Map<Player, PlayerTimer> playerTimers;

	private final Stock<GameChange> changes;

	private final AtomicReference<TurnInfo> currentTurnInfo;

	public static TimedGame create(TimedGameSettings settings) {
		Game game = settings.getWrappedGameSettings().createGame();
		return new TimedGame(game, settings);
	}

	private TimedGame(Game game, TimedGameSettings settings) {
		this.settings = settings;
		this.changes = Stock.forObject();
		this.currentTurnInfo = new AtomicReference<>();
		this.game = game;
		this.turnDurationSeconds = settings.getTurnDurationSeconds();
		this.turnTimer = new SingleTaskTimer();
		this.playerTimers = new HashMap<>();
	}

	@Override
	public RestorableGameSettings getSettings() {
		return settings;
	}

	@Override
	public void addPlayer(GamePlayer player) {
		game.addPlayer(player);
	}

	@Override
	public boolean removePlayer(Player player) {
		return game.removePlayer(player);
	}

	@Override
	public void shufflePLayers() {
		game.shufflePLayers();
	}

	@Override
	public void start() {
		synchronized (getLock()) {
			game.start();

			game.getPlayersWithColors()
					.stream()
					.map(GamePlayer::getRealPlayer)
					.forEach(player -> playerTimers.put(player, new PlayerTimer(player,
							TimeUnit.SECONDS.toMillis(settings.getPlayerTurnTotalDurationSeconds()))));

			applyChangesTransformer();

			Player firstPlayer = game.getCurrentPlayer().getRealPlayer();
			runTurnTimer(firstPlayer, turnDurationSeconds);

			completed().subscribe(ignored -> onComplete());
		}
	}

	@Override
	public ReadOnlyGameBoard getBoard() {
		return game.getBoard();
	}

	@Override
	public void restore(List<GameChange> changes) {
		synchronized (getLock()) {
			game.start();

			game.getPlayersWithColors()
					.stream()
					.map(GamePlayer::getRealPlayer)
					.forEach(player -> playerTimers.put(player, new PlayerTimer(player,
							TimeUnit.SECONDS.toMillis(settings.getPlayerTurnTotalDurationSeconds()))));

			ChangeVisitor changeVisitor = new ChangeVisitor();
			for (GameChange gameChange : changes) {
				gameChange.accept(changeVisitor);
			}

			applyChangesTransformer();
			if (!game.isCompleted()) {
				Player firstPlayer = game.getCurrentPlayer().getRealPlayer();
				runTurnTimer(firstPlayer, turnDurationSeconds);
			}
			completed().subscribe(ignored -> onComplete());
		}
	}

	@Override
	public boolean isStarted() {
		return game.isStarted();
	}

	@Override
	public CellChange turn(Point point) {
		synchronized (getLock()) {
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
	public List<Player> getPlayers() {
		return game.getPlayers();
	}

	@Override
	public List<GamePlayer> getPlayersWithColors() {
		return game.getPlayersWithColors();
	}

	@Override
	public List<GamePlayer> getActivePlayers() {
		return game.getActivePlayers();
	}

	@Override
	public GamePlayer getCurrentPlayer() {
		return game.getCurrentPlayer();
	}

	@Override
	public Optional<GamePlayer> ownerOfPoint(Point point) {
		return game.ownerOfPoint(point);
	}

	@Override
	public Statistics getStatistics() {
		return game.getStatistics();
	}

	@Override
	public List<GamePlayer> getLosers() {
		return game.getLosers();
	}

	@Override
	public Optional<GamePlayer> getWinner() {
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
	public MonoObservable<TimedGame> completed() {
		return changes.asObservable().filter(change -> change instanceof GameComplete).map(state -> this).toMono();
	}

	@Override
	public boolean isCompleted() {
		return game.isCompleted();
	}

	private void runTurnTimer(Player player, int seconds) {
		turnTimer.schedule(new TurnTimerTask(player), Duration.ofSeconds(seconds).toMillis());
		playerTimers.get(player).resume();
	}

	private void applyChangesTransformer() {
		game.changes().asObservable().map(gameChange -> {
			if (gameChange instanceof PlayerTurn) {
				PlayerTurn playerTurn = (PlayerTurn) gameChange;
				TurnInfo turnInfo = currentTurnInfo.get();
				return new PlayerTimedTurn(playerTurn.getPoint(), turnInfo.getRemainingTurnMillis(),
						turnInfo.getRemainingPlayerTotalTurnMillis());
			}
			return gameChange;

		}).subscribe(changes::add);
	}

	private void onComplete() {
		changes.close();
		turnTimer.destroy();
		playerTimers.values().forEach(PauseableTimer::destroy);
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

	@Override
	public Object getLock() {
		return game.getLock();
	}

	private final class TurnTimerTask implements Runnable {

		private final Player player;

		private TurnTimerTask(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			synchronized (getLock()) {
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
				synchronized (getLock()) {
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

	private final class ChangeVisitor implements GameChangeVisitor {

		@Override
		public void visit(PlayerTurn playerTurn) {
			throw new UnsupportedChangeException(playerTurn.getClass().toString());
		}

		@Override
		public void visit(PlayerTimedTurn playerTimedTurn) {
			currentTurnInfo.set(new TurnInfo(playerTimedTurn.getRemainingTurnMillis(),
					playerTimedTurn.getRemainingPlayerTotalTurnMillis()));
			game.turn(playerTimedTurn.getPoint());
		}

		@Override
		public void visit(PlayerKick playerKick) {
			game.kick(playerKick.getPlayerWithPoints().getPlayer().getRealPlayer());
		}

		@Override
		public void visit(GameComplete gameComplete) {

		}
	}
}
