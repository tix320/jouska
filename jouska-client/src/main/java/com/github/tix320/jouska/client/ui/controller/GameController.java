package com.github.tix320.jouska.client.ui.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.client.ui.game.Tile;
import com.github.tix320.jouska.client.ui.helper.component.TimerLabel;
import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameBoards;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.PauseableTimer;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.IN_GAME_SERVICE;
import static java.util.stream.Collectors.toMap;

public class GameController implements Controller<StartGameCommand> {

	@FXML
	private AnchorPane mainPane;

	@FXML
	private FlowPane gameBoardPane;

	@FXML
	private Circle turnIndicator;

	@FXML
	private TimerLabel turnTimeIndicator;

	@FXML
	private TimerLabel turnTotalTimeIndicator;

	@FXML
	private TimerLabel gameDurationIndicator;

	@FXML
	private Label gameNameLabel;

	@FXML
	private VBox statisticsBoard;

	@FXML
	private Label loseWinLabel;

	private Map<PlayerColor, PauseableTimer> playerTurnTimers;

	private Tile[][] tiles;
	private Map<PlayerColor, HBox> statisticsNodes;

	private long gameId;
	private Game game;
	private TimedGameSettings gameSettings;
	private InGamePlayer myPlayer;

	private SimpleObjectProperty<PlayerColor> turnProperty = new SimpleObjectProperty<>();

	private AtomicBoolean turned = new AtomicBoolean();

	private BlockingQueue<GameChangeDto> changesQueue;

	private MonoPublisher<None> destroyPublisher = Publisher.mono();

	@Override
	public void init(StartGameCommand startGameCommand) {
		this.gameId = startGameCommand.getGameId();
		this.gameSettings = startGameCommand.getGameSettings();

		List<InGamePlayer> players = startGameCommand.getPlayers();
		GameBoard gameBoard = GameBoards.createByType(gameSettings.getBoardType(),
				players.stream().map(InGamePlayer::getColor).collect(Collectors.toList()));

		this.game = SimpleGame.createPredefined(gameBoard, players);

		this.myPlayer = new InGamePlayer(CurrentUserContext.getPlayer(), startGameCommand.getMyPlayer());

		this.gameNameLabel.setText("Game: " + gameSettings.getName());
		this.changesQueue = new LinkedBlockingQueue<>();

		createStatisticsBoardComponent(players);
		BoardCell[][] matrix = gameBoard.getMatrix();
		createBoardComponent(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();

		DateTimeFormatter timersFormatter = DateTimeFormatter.ofPattern("mm:ss");
		turnTimeIndicator.setFormatter(timersFormatter);
		turnTotalTimeIndicator.setFormatter(timersFormatter);
		gameDurationIndicator.setFormatter(timersFormatter);

		initTotalTurnTimers(players);

		runBoardChangesConsumer();
		listenChanges();

		game.start();

		InGamePlayer firstPlayer = players.get(0);
		turnProperty.set(firstPlayer.getColor());
		startTurnTimer();
		runGameTimer();

		updateStatistics(game.getStatistics().summaryPoints());
		turned.set(false);
	}

	private void listenChanges() {
		MonoObservable<?> destroy = destroyPublisher.asObservable();

		game.completed().takeUntil(destroy).subscribe(none -> onGameComplete());

		destroy.subscribe(Subscriber.builder().onComplete(completionType -> {
			turnTimeIndicator.stop();
			turnTotalTimeIndicator.stop();
			gameDurationIndicator.stop();
		}));

		IN_GAME_SERVICE.changes(gameId).takeUntil(destroy).subscribe(changesQueue::add);
	}

	private void runBoardChangesConsumer() {
		new Thread(() -> {
			while (true) {
				GameChangeDto gameChange = Try.supplyOrRethrow(() -> changesQueue.take());
				if (gameChange instanceof PlayerTurnDto) {
					PlayerTurnDto playerTurn = (PlayerTurnDto) gameChange;
					turn(playerTurn);
					List<InGamePlayer> losers = game.getLostPlayers();
					if (losers.contains(myPlayer)) {
						showLose();
					}
				}
				else if (gameChange instanceof PlayerLeaveDto) {
					PlayerLeaveDto playerLeave = (PlayerLeaveDto) gameChange;
					kickPlayer(playerLeave);
				}
				else if (gameChange instanceof GameCompleteDto) {
					GameCompleteDto gameComplete = (GameCompleteDto) gameChange;
					if (!game.isCompleted()) {
						game.forceCompleteGame(gameComplete.getWinner().getRealPlayer());
					}
				}
				else if (gameChange instanceof GameTimeDrawCompletionDto) {
					GameTimeDrawCompletionDto drawComplete = (GameTimeDrawCompletionDto) gameChange;
					Platform.runLater(() -> {
						gameDurationIndicator.stop();
						gameDurationIndicator.setTextFill(Color.RED);
						gameDurationIndicator.setTime(LocalTime.ofSecondOfDay(drawComplete.getAdditionalSeconds()));
						gameDurationIndicator.run();
					});
				}
				else {
					throw new IllegalArgumentException();
				}
				turned.set(false);
			}
		}).start();
	}

	@Override
	public void destroy() {
		if (!destroyPublisher.isCompleted()) {
			destroyPublisher.complete();
		}
	}

	@FXML
	private void onFullScreenClick() {
		Stage stage = JouskaUI.stage;
		if (stage.isFullScreen()) {
			stage.setFullScreen(false);
		}
		else {
			stage.setFullScreen(true);
		}
	}

	private void createStatisticsBoardComponent(List<InGamePlayer> players) {
		statisticsNodes = new EnumMap<>(PlayerColor.class);
		for (InGamePlayer player : players) {
			HBox hBox = new HBox();
			hBox.setSpacing(20);

			Circle playerIcon = new Circle(16);
			playerIcon.setTranslateY(10);
			playerIcon.setFill(Color.valueOf(player.getColor().getColorCode()));
			playerIcon.setStroke(Color.BLACK);

			Label nicknameLabel = new Label();
			nicknameLabel.setTextFill(Color.web(player.getColor().getColorCode()));
			nicknameLabel.setAlignment(Pos.TOP_CENTER);
			nicknameLabel.setFont(Font.font("MV Boli", 30));
			nicknameLabel.setText(player.getRealPlayer().getNickname());
			if (player.equals(myPlayer)) {
				nicknameLabel.setBorder(new Border(
						new BorderStroke(Color.web("#948fff"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
								BorderWidths.DEFAULT)));
			}

			Label pointsLabel = new Label();
			pointsLabel.setTextFill(Color.web(player.getColor().getColorCode()));
			pointsLabel.setAlignment(Pos.TOP_CENTER);
			pointsLabel.setFont(Font.font("MV Boli", 30));

			hBox.getChildren().addAll(playerIcon, pointsLabel, nicknameLabel);

			statisticsNodes.put(player.getColor(), hBox);
			statisticsBoard.getChildren().add(hBox);
		}
	}

	private void initTotalTurnTimers(List<InGamePlayer> players) {
		playerTurnTimers = players.stream()
				.map(InGamePlayer::getColor)
				.collect(toMap(color -> color, color -> new PauseableTimer(
						TimeUnit.SECONDS.toMillis(gameSettings.getPlayerTurnTotalDurationSeconds()), () -> {
				})));

		turnProperty.addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				playerTurnTimers.get(oldValue).pause();
			}
			PauseableTimer timer = playerTurnTimers.get(newValue);
			long remainingMilliSeconds = timer.getRemainingMilliSeconds();
			turnTotalTimeIndicator.setTime(
					LocalTime.ofSecondOfDay(TimeUnit.MILLISECONDS.toSeconds(remainingMilliSeconds)));
			turnTotalTimeIndicator.run();
			timer.resume();
		});
	}

	private void startTurnTimer() {
		Platform.runLater(() -> {
			int turnDurationSeconds = gameSettings.getTurnDurationSeconds();
			turnTimeIndicator.setTime(LocalTime.of(0, 0, turnDurationSeconds));
			turnTimeIndicator.run();
		});
	}

	private void runGameTimer() {
		int gameDurationMinutes = gameSettings.getGameDurationMinutes();
		gameDurationIndicator.setTime(LocalTime.of(0, gameDurationMinutes));
		gameDurationIndicator.run();
	}

	public void turn(PlayerTurnDto playerTurnDto) {
		Point point = playerTurnDto.getPoint();
		CellChange rootChange = game.turn(point);
		animateCellChanges(rootChange);
		Map<InGamePlayer, Integer> statistics = game.getStatistics().summaryPoints();
		updateStatistics(statistics);
		InGamePlayer currentPlayer = game.getCurrentPlayer();
		Platform.runLater(() -> turnProperty.set(currentPlayer.getColor()));
		startTurnTimer();
	}

	public void kickPlayer(PlayerLeaveDto playerLeaveDto) {
		Player player = playerLeaveDto.getPlayer();
		PlayerWithPoints playerWithPoints = game.kick(player);
		animateLeave(playerWithPoints);
		Map<InGamePlayer, Integer> statistics = game.getStatistics().summaryPoints();
		updateStatistics(statistics);
	}

	private void onGameComplete() {
		InGamePlayer winner = game.getWinner().orElseThrow();
		List<InGamePlayer> losers = game.getLostPlayers();
		if (losers.contains(myPlayer)) {
			showLose();
		}

		resetTimersToZero();
		Platform.runLater(() -> {
			if (myPlayer.equals(winner)) {
				showWin();
			}
		});
	}

	private void resetTimersToZero() {
		turnTimeIndicator.stop();
		turnTotalTimeIndicator.stop();
		gameDurationIndicator.stop();
		Platform.runLater(() -> {
			turnTimeIndicator.setTime(LocalTime.MIDNIGHT);
			turnTimeIndicator.setTextFill(Color.RED);
			turnTotalTimeIndicator.setTime(LocalTime.MIDNIGHT);
			turnTotalTimeIndicator.setTextFill(Color.RED);
			gameDurationIndicator.setTime(LocalTime.MIDNIGHT);
			gameDurationIndicator.setTextFill(Color.RED);
		});
	}

	public void showLose() {
		Platform.runLater(() -> {
			Transition transition = appearLoseWinLabel("You lose", Color.RED);
			transition.play();
		});
	}

	public void showWin() {
		Platform.runLater(() -> {
			Transition transition = appearLoseWinLabel("You win", Color.valueOf(myPlayer.getColor().getColorCode()));
			transition.play();
		});
	}

	public void animateLeave(PlayerWithPoints playerWithPoints) {
		List<Point> pointsBelongedToPlayer = playerWithPoints.getPoints();
		Transition[] transitions = pointsBelongedToPlayer.stream()
				.map(point -> makeTileTransition(point, new BoardCell(null, 0)))
				.toArray(Transition[]::new);

		ParallelTransition fullTransition = new ParallelTransition(transitions);
		Publisher<None> onFinishPublisher = Publisher.mono();
		fullTransition.setOnFinished(event -> onFinishPublisher.publish(None.SELF));
		fullTransition.play();
		onFinishPublisher.asObservable().blockUntilComplete();
	}

	private Transition appearLoseWinLabel(String text, Color color) {
		loseWinLabel.setVisible(true);
		loseWinLabel.setText(text);
		loseWinLabel.setTextFill(color);
		loseWinLabel.setFont(Font.font("MV Boli", 12));
		ScaleTransition transition = new ScaleTransition(Duration.seconds(1), loseWinLabel);
		transition.setFromX(1);
		transition.setToX(4);
		transition.setFromY(1);
		transition.setToY(4);
		return transition;
	}

	private void updateStatistics(Map<InGamePlayer, Integer> playerSummaryPoints) {
		playerSummaryPoints.forEach((player, points) -> {
			Label label = (Label) statisticsNodes.get(player.getColor()).getChildren().get(1);
			Platform.runLater(() -> label.setText(points + " -"));
		});
	}

	private void createBoardComponent(int rows, int columns) {
		gameBoardPane.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));
		turnProperty.addListener((observable, oldValue, newValue) -> {
			Border border = new Border(
					new BorderStroke(Color.valueOf(turnProperty.get().getColorCode()), BorderStrokeStyle.SOLID,
							CornerRadii.EMPTY, new BorderWidths(5)));
			gameBoardPane.setBorder(border);
		});
		double boardWidth = columns * Tile.PREF_SIZE + 10;
		double boardHeight = rows * Tile.PREF_SIZE + 10;
		gameBoardPane.setPrefWidth(boardWidth);
		gameBoardPane.setPrefHeight(boardHeight);
		mainPane.setPrefWidth(boardWidth + 400);
		tiles = new Tile[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				Tile tile = new Tile();
				tile.setLayoutX(i * 100);
				tile.setLayoutY(j * 100);
				tiles[i][j] = tile;
				int x = i;
				int y = j;
				tile.setOnMouseClicked(event -> onTileClick(x, y));
				gameBoardPane.getChildren().add(tile);
			}
		}
	}

	private void initTurnIndicator() {
		turnProperty.addListener(
				(observable, oldValue, newValue) -> turnIndicator.setFill(Color.valueOf(newValue.getColorCode())));

		FadeTransition transition = new FadeTransition(Duration.seconds(0.6), turnIndicator);
		transition.setFromValue(0.3);
		transition.setToValue(1);
		transition.setCycleCount(Timeline.INDEFINITE);
		transition.setAutoReverse(true);
		transition.play();
		destroyPublisher.asObservable().subscribe(Subscriber.builder().onComplete(completionType -> transition.stop()));
	}

	private void fillBoard(BoardCell[][] board) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				BoardCell boardCell = board[i][j];
				if (boardCell.getColor() != null) {
					tiles[i][j].makeAppearTransition(boardCell.getColor(), boardCell.getPoints()).play();
				}
			}
		}
	}

	private void onTileClick(int x, int y) {
		if (game.isCompleted() || !turnProperty.get().equals(myPlayer.getColor()) || !turned.compareAndSet(false,
				true)) {
			return;
		}

		Point point = new Point(x, y);
		game.ownerOfPoint(point).ifPresentOrElse(player -> {
			if (myPlayer.equals(player)) {
				IN_GAME_SERVICE.turn(gameId, point);
				turned.set(true);
			}
			else {
				turned.set(false);
			}
		}, () -> turned.set(false));
	}

	private void animateCellChanges(CellChange root) {
		SequentialTransition fullAnimation = new SequentialTransition();

		Publisher<None> onFinishPublisher = Publisher.mono();
		fullAnimation.setOnFinished(event -> onFinishPublisher.publish(None.SELF));

		fullAnimation.getChildren().add(makeTileTransition(root.getPoint(), root.getBoardCell()));

		if (root.getChildren().isEmpty()) {
			fullAnimation.play();
			onFinishPublisher.asObservable().blockUntilComplete();
			return;
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

			SequentialTransition transition = new SequentialTransition();

			if (cellChange.isCollapse()) {
				if (cellChange.getChildren().size() != 1) {
					throw new IllegalStateException("Illegal size: " + cellChange.getChildren().size());
				}

				CellChange collapsingChange = cellChange.getChildren().get(0);
				changesStack.addAll(collapsingChange.getChildren());
				cellChange = collapsingChange;
				transition.getChildren().add(makeTileTransition(cellChange.getPoint(), cellChange.getBoardCell()));
			}

			ParallelTransition childrenTransition = new ParallelTransition();
			for (CellChange child : cellChange.getChildren()) {
				Transition childTransition = makeTileTransition(child.getPoint(), child.getBoardCell());
				childrenTransition.getChildren().add(childTransition);
			}
			transition.getChildren().add(childrenTransition);

			fullAnimation.getChildren().add(transition);
		}

		fullAnimation.play();
		onFinishPublisher.asObservable().blockUntilComplete();
	}

	private Transition makeTileTransition(Point point, BoardCell boardCell) {
		int i = point.i;
		int j = point.j;
		Tile tile = tiles[i][j];
		int points = boardCell.getPoints();
		PlayerColor player = boardCell.getColor();
		switch (points) {
			case 0:
				return tile.makeDisappearTransition();
			case 1:
				return tile.makeAppearTransition(player, points);
			case 2:
			case 3:
			case 4:
				return tile.makeDisAppearAndAppearTransition(player, points);
			default:
				throw new IllegalStateException(points + "");
		}
	}

	public void onLeaveClick() {
		destroyPublisher.complete();
		if (!game.isCompleted()) {
			IN_GAME_SERVICE.leave(gameId);
		}
		JouskaUI.switchComponent(ComponentType.MENU);
	}
}
