package com.github.tix320.jouska.client.ui.controller;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.client.infrastructure.event.EventDispatcher;
import com.github.tix320.jouska.client.infrastructure.event.game.CanTurnEvent;
import com.github.tix320.jouska.client.infrastructure.event.game.ForceCompleteGameEvent;
import com.github.tix320.jouska.client.infrastructure.event.game.LeaveEvent;
import com.github.tix320.jouska.client.infrastructure.event.game.TurnEvent;
import com.github.tix320.jouska.client.ui.game.Tile;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.Game.CellChange;
import com.github.tix320.jouska.core.game.Game.PlayerWithPoints;
import com.github.tix320.jouska.core.game.SimpleGame;
import com.github.tix320.jouska.core.model.*;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
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
	private FlowPane gameBoard;

	@FXML
	private Circle turnIndicator;

	@FXML
	private Label turnTimeIndicator;

	@FXML
	private Label gameDurationIndicator;

	@FXML
	private Label gameName;

	@FXML
	private VBox statisticsBoard;

	@FXML
	private Label loseWinLabel;

	private Timeline currentTurnTimer;
	private Timeline gameTimer;

	private Map<PlayerColor, HBox> statisticsNodes;
	private Tile[][] tiles;

	private long gameId;
	private Game game;
	private InGamePlayer myPlayer;

	private SimpleObjectProperty<PlayerColor> turnProperty = new SimpleObjectProperty<>();
	private SimpleBooleanProperty activeBoard = new SimpleBooleanProperty();

	private MonoPublisher<None> destroyPublisher = Publisher.mono();
	private Lock gameLock = new ReentrantLock();

	@Override
	public void init(StartGameCommand startGameCommand) {
		GameSettings gameSettings = startGameCommand.getGameSettings();
		gameId = startGameCommand.getGameId();
		game = SimpleGame.create(gameSettings, startGameCommand.getGameBoard(), startGameCommand.getPlayers());
		myPlayer = new InGamePlayer(CurrentUserContext.getPlayer(), startGameCommand.getMyPlayer());

		gameName.setText("Game: " + gameSettings.getName());

		PlayerColor firstPlayer = startGameCommand.getPlayers().get(0).getColor();
		turnProperty.set(firstPlayer);
		turnIndicator.fillProperty()
				.bind(Bindings.createObjectBinding(() -> Color.valueOf(turnProperty.get().getColorCode()),
						turnProperty));

		initStatisticsBoard();
		GameBoard gameBoard = startGameCommand.getGameBoard();
		CellInfo[][] matrix = gameBoard.getMatrix();
		initBoard(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();

		MonoObservable<?> destroy = destroyPublisher.asObservable();

		game.turns().takeUntil(destroy).subscribe(rootChange -> animateCellChanges(rootChange).blockUntilComplete());

		game.lostPlayers().takeUntil(destroy).subscribe(this::handleLose);

		game.kickedPlayers().takeUntil(destroy).subscribe(player -> {
			animateKick(player).blockUntilComplete();
			resetTurn();
		});

		game.completed().takeUntil(destroy).subscribe(ignored -> game.winner().subscribe(this::handleWin));

		game.start();
		game.getStatistics().summaryPoints().toMono().subscribe(this::updateStatistics);

		EventDispatcher.on(CanTurnEvent.class).takeUntil(destroy).subscribe(canTurnEvent -> resetTurn());
		EventDispatcher.on(TurnEvent.class).takeUntil(destroy).subscribe(turnEvent -> turn(turnEvent.getPoint()));
		EventDispatcher.on(LeaveEvent.class).takeUntil(destroy).subscribe(leaveEvent -> leave(leaveEvent.getPlayer()));
		EventDispatcher.on(ForceCompleteGameEvent.class)
				.takeUntil(destroy)
				.subscribe(leaveEvent -> forceComplete(leaveEvent.getWinner()));

		initGameTimer();
		initTurnTimer();
		JouskaUI.onExit().subscribe(none -> leaveGame());
	}

	@Override
	public void destroy() {
		destroyPublisher.complete();
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

	private void initStatisticsBoard() {
		List<InGamePlayer> players = game.getPlayers();
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
			nicknameLabel.setText(player.getPlayer().getNickname());

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

		int turnDurationSeconds = game.getSettings().getTurnDurationSeconds();
		currentTurnTimer.setCycleCount(turnDurationSeconds);
	}

	private void startTurnTimer() {
		Platform.runLater(() -> {
			int turnDurationSeconds = game.getSettings().getTurnDurationSeconds();
			turnTimeIndicator.setText(String.valueOf(turnDurationSeconds));
			currentTurnTimer.jumpTo(Duration.ZERO);
			currentTurnTimer.play();
		});
	}

	private void initGameTimer() {
		int gameDurationMinutes = game.getSettings().getGameDurationMinutes();
		LocalTime endTime = LocalTime.now().plusMinutes(gameDurationMinutes);
		gameTimer = new Timeline(new KeyFrame(Duration.seconds(1), ae -> {
			LocalTime now = LocalTime.now();
			LocalTime currentTime = endTime.minusSeconds(now.getMinute() * 60 + now.getSecond());
			gameDurationIndicator.setText(currentTime.getMinute() + ":" + currentTime.getSecond());
		}));

		gameTimer.setCycleCount(gameDurationMinutes * 60);
		gameTimer.play();
	}

	public void turn(Point point) {
		try {
			gameLock.lock();
			game.turn(point);
			game.getStatistics().summaryPoints().toMono().subscribe(this::updateStatistics);
		}
		finally {
			gameLock.unlock();
		}
	}

	public void leave(Player player) {
		try {
			gameLock.lock();
			game.kick(player);
			game.getStatistics().summaryPoints().toMono().subscribe(this::updateStatistics);
		}
		finally {
			gameLock.unlock();
		}
	}

	public void forceComplete(Player winner) {
		try {
			gameLock.lock();
			if (!game.isCompleted()) {
				game.forceCompleteGame(winner);
			}
		}
		finally {
			gameLock.unlock();
		}
	}

	public void handleWin(InGamePlayer player) {
		turnIndicator.setFill(Color.GRAY);
		if (player.getColor() == myPlayer.getColor()) {
			Platform.runLater(
					() -> appearLoseWinLabel("You win", Color.valueOf(myPlayer.getColor().getColorCode())).play());
		}
		currentTurnTimer.stop();
		gameTimer.stop();
		Platform.runLater(() -> {
			turnTimeIndicator.setText("0");
			turnTimeIndicator.setTextFill(Color.RED);
			gameDurationIndicator.setText("00:00");
			gameDurationIndicator.setTextFill(Color.RED);
		});
	}

	public void handleLose(InGamePlayer player) {
		turnProperty.set(game.getCurrentPlayer().getColor());
		if (player.getColor() == myPlayer.getColor()) {
			Platform.runLater(() -> {
				Transition transition = appearLoseWinLabel("You lose", Color.RED);
				transition.play();
			});
		}
	}


	public Observable<None> animateKick(PlayerWithPoints playerWithPoints) {
		List<Point> pointsBelongedToPlayer = playerWithPoints.points;
		Transition[] transitions = pointsBelongedToPlayer.stream()
				.map(point -> makeTileTransition(point, new CellInfo(null, 0)))
				.toArray(Transition[]::new);

		ParallelTransition fullTransition = new ParallelTransition(transitions);
		Publisher<None> onFinishPublisher = Publisher.simple();
		fullTransition.setOnFinished(event -> onFinishPublisher.publish(None.SELF));
		fullTransition.play();
		return onFinishPublisher.asObservable();
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

	private void resetTurn() {
		if (game.isCompleted()) {
			return;
		}
		Platform.runLater(() -> {
			turnProperty.set(game.getCurrentPlayer().getColor());
			if (game.getCurrentPlayer().getColor() == myPlayer.getColor()) {
				activeBoard.set(true);
			}
		});

		startTurnTimer();
	}

	private void initBoard(int rows, int columns) {
		gameBoard.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));
		gameBoard.disableProperty().bind(activeBoard.not());
		gameBoard.borderProperty()
				.bind(Bindings.createObjectBinding(() -> new Border(
						new BorderStroke(Color.valueOf(game.getCurrentPlayer().getColor().getColorCode()),
								BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))), turnProperty));
		double boardWidth = columns * Tile.PREF_SIZE + 10;
		double boardHeight = rows * Tile.PREF_SIZE + 10;
		gameBoard.setPrefWidth(boardWidth);
		gameBoard.setPrefHeight(boardHeight);
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
				gameBoard.getChildren().add(tile);
			}
		}
	}

	private void initTurnIndicator() {
		FadeTransition transition = new FadeTransition(Duration.seconds(0.6), turnIndicator);
		transition.setFromValue(0.3);
		transition.setToValue(1);
		transition.setCycleCount(Timeline.INDEFINITE);
		transition.setAutoReverse(true);
		transition.play();
	}

	private void fillBoard(CellInfo[][] board) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				CellInfo cellInfo = board[i][j];
				if (cellInfo.getPlayer() != null) {
					tiles[i][j].appearTransition(cellInfo.getPlayer(), cellInfo.getPoints()).play();
				}
			}
		}
	}

	private void onTileClick(int x, int y) {
		Point point = new Point(x, y);
		if (myPlayer.equals(game.ownerOfPoint(point))) {
			activeBoard.set(false);
			IN_GAME_SERVICE.turn(gameId, point);
		}
	}

	private MonoObservable<None> animateCellChanges(CellChange root) {
		SequentialTransition fullAnimation = new SequentialTransition();

		Publisher<None> onFinishPublisher = Publisher.simple();
		fullAnimation.setOnFinished(event -> onFinishPublisher.publish(None.SELF));

		fullAnimation.getChildren().add(makeTileTransition(root.point, root.cellInfo));

		if (root.children.isEmpty()) {
			fullAnimation.play();
			return onFinishPublisher.asObservable().toMono();
		}

		if (root.children.size() != 1) {
			throw new IllegalStateException("Illegal size: " + root.children.size());
		}

		Deque<CellChange> changesStack = new LinkedList<>();
		changesStack.add(root);
		while (!changesStack.isEmpty()) {
			CellChange cellChange = changesStack.removeFirst();

			if (cellChange.children.isEmpty()) {
				continue;
			}

			SequentialTransition transition = new SequentialTransition();

			if (cellChange.collapse) {
				if (cellChange.children.size() != 1) {
					throw new IllegalStateException("Illegal size: " + cellChange.children.size());
				}

				CellChange collapsingChange = cellChange.children.get(0);
				changesStack.addAll(collapsingChange.children);
				cellChange = collapsingChange;
				transition.getChildren().add(makeTileTransition(cellChange.point, cellChange.cellInfo));
			}

			ParallelTransition childrenTransition = new ParallelTransition();
			for (CellChange child : cellChange.children) {
				Transition childTransition = makeTileTransition(child.point, child.cellInfo);
				childrenTransition.getChildren().add(childTransition);
			}
			transition.getChildren().add(childrenTransition);

			fullAnimation.getChildren().add(transition);
		}

		fullAnimation.play();
		return onFinishPublisher.asObservable().toMono();
	}

	private Transition makeTileTransition(Point point, CellInfo cellInfo) {
		int i = point.i;
		int j = point.j;
		Tile tile = tiles[i][j];
		int points = cellInfo.getPoints();
		PlayerColor player = cellInfo.getPlayer();
		switch (points) {
			case 0:
				return tile.disappearTransition();
			case 1:
				return tile.appearTransition(player, points);
			case 2:
			case 3:
			case 4:
				return tile.disAppearAndAppearTransition(player, points);
			default:
				throw new IllegalStateException(points + "");
		}
	}

	public void onLeaveClick() {
		leaveGame();
		JouskaUI.switchComponent(ComponentType.MENU);
	}

	public void leaveGame() {
		IN_GAME_SERVICE.leave(gameId);
	}
}
