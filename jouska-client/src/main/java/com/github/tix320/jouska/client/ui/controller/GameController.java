package com.github.tix320.jouska.client.ui.controller;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.client.ui.game.Tile;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.game.*;
import com.github.tix320.jouska.core.game.creation.GameBoards;
import com.github.tix320.jouska.core.game.creation.GameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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

public class GameController implements Controller<StartGameCommand> {

	@FXML
	private AnchorPane mainPane;

	@FXML
	private FlowPane gameBoardPane;

	@FXML
	private Circle turnIndicator;

	@FXML
	private Label turnTimeIndicator;

	@FXML
	private Label gameDurationIndicator;

	@FXML
	private Label gameNameLabel;

	@FXML
	private VBox statisticsBoard;

	@FXML
	private Label loseWinLabel;

	private Timeline currentTurnTimer;
	private Timeline gameTimer;

	private Tile[][] tiles;
	private Map<PlayerColor, HBox> statisticsNodes;

	private long gameId;
	private Game game;
	private GameSettings gameSettings;
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

		InGamePlayer firstPlayer = players.get(0);
		turnProperty.set(firstPlayer.getColor());

		createStatisticsBoardComponent(players);
		BoardCell[][] matrix = gameBoard.getMatrix();
		createBoardComponent(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();

		initGameTimer();
		initTurnTimer();

		runBoardChangesConsumer();
		startTurnTimer();
		listenChanges();

		game.start();
		updateStatistics(game.getStatistics().summaryPoints());
		turned.set(false);
	}

	private void listenChanges() {
		MonoObservable<?> destroy = destroyPublisher.asObservable();

		game.completed().takeUntil(destroy).subscribe(none -> onGameComplete());

		destroy.subscribe(Subscriber.builder().onComplete(completionType -> {
			if (gameTimer != null) {
				gameTimer.stop();
			}
			if (currentTurnTimer != null) {
				currentTurnTimer.stop();
			}
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

	private void initTurnTimer() {
		currentTurnTimer = new Timeline(new KeyFrame(Duration.seconds(1),
				ae -> turnTimeIndicator.setText(String.valueOf(Integer.parseInt(turnTimeIndicator.getText()) - 1))));

		int turnDurationSeconds = gameSettings.getTurnDurationSeconds();
		currentTurnTimer.setCycleCount(turnDurationSeconds);
	}

	private void startTurnTimer() {
		Platform.runLater(() -> {
			int turnDurationSeconds = gameSettings.getTurnDurationSeconds();
			turnTimeIndicator.setText(String.valueOf(turnDurationSeconds));
			currentTurnTimer.jumpTo(Duration.ZERO);
			currentTurnTimer.play();
		});
	}

	private void initGameTimer() {
		int gameDurationMinutes = gameSettings.getGameDurationMinutes();
		LocalTime endTime = LocalTime.now().plusMinutes(gameDurationMinutes);
		gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
			LocalTime now = LocalTime.now();
			LocalTime currentTime = endTime.minusSeconds(now.getMinute() * 60 + now.getSecond());
			gameDurationIndicator.setText(currentTime.getMinute() + ":" + currentTime.getSecond());
		}));

		gameTimer.setCycleCount(gameDurationMinutes * 60);
		gameTimer.play();
	}

	public void turn(PlayerTurnDto playerTurnDto) {
		Point point = playerTurnDto.getPoint();
		CellChange rootChange = game.turn(point);
		animateCellChanges(rootChange);
		Map<InGamePlayer, Integer> statistics = game.getStatistics().summaryPoints();
		updateStatistics(statistics);
		InGamePlayer currentPlayer = game.getCurrentPlayer();
		turnProperty.set(currentPlayer.getColor());
		startTurnTimer();
	}

	public void kickPlayer(PlayerLeaveDto playerLeaveDto) {
		Player player = playerLeaveDto.getPlayer();
		PlayerWithPoints playerWithPoints = game.kick(player);
		animateLeave(playerWithPoints);
		Map<InGamePlayer, Integer> statistics = game.getStatistics().summaryPoints();
		updateStatistics(statistics);
	}

	public void onGameComplete() {
		InGamePlayer winner = game.getWinner().orElseThrow();
		List<InGamePlayer> losers = game.getLostPlayers();
		if (losers.contains(myPlayer)) {
			showLose();
		}

		currentTurnTimer.stop();
		gameTimer.stop();
		Platform.runLater(() -> {
			if (myPlayer.equals(winner)) {
				showWin();
			}
			turnTimeIndicator.setText("0");
			turnTimeIndicator.setTextFill(Color.RED);
			gameDurationIndicator.setText("00:00");
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
		gameBoardPane.borderProperty()
				.bind(Bindings.createObjectBinding(() -> new Border(
						new BorderStroke(Color.valueOf(turnProperty.get().getColorCode()), BorderStrokeStyle.SOLID,
								CornerRadii.EMPTY, new BorderWidths(5))), turnProperty));
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
		turnIndicator.fillProperty()
				.bind(Bindings.createObjectBinding(() -> Color.valueOf(turnProperty.get().getColorCode()),
						turnProperty));

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
