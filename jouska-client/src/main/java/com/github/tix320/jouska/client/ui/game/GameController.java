package com.github.tix320.jouska.client.ui.game;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.Lock;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.ui.Controller;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.game.JouskaGame.CellChange;
import com.github.tix320.jouska.core.game.JouskaGame.PlayerWithPoints;
import com.github.tix320.jouska.core.game.SimpleJouskaGame;
import com.github.tix320.jouska.core.game.TimedJouskaGame;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import static com.github.tix320.jouska.client.app.Services.IN_GAME_SERVICE;

public class GameController implements Controller<StartGameCommand> {

	public static GameController CURRENT;

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
	private Circle myColor;

	@FXML
	private Label gameName;

	@FXML
	private VBox statisticsBoard;

	@FXML
	private Label loseWinLabel;

	@FXML
	private Button leaveButton;

	private Timeline currentTurnTimer;

	private Timeline gameTimer;

	private Map<Player, HBox> statisticsNodes;

	private Tile[][] tiles;

	private long gameId;

	private Player myPlayer;

	private SimpleObjectProperty<Player> turnProperty = new SimpleObjectProperty<>();

	private SimpleBooleanProperty activeBoard = new SimpleBooleanProperty();

	private JouskaGame game;

	private int turnTimeSeconds;

	private int gameDurationMinutes;

	@Override
	public void initialize(StartGameCommand startGameCommand) {
		CURRENT = this;
		game = TimedJouskaGame.create(
				SimpleJouskaGame.create(startGameCommand.getGameBoard(), startGameCommand.getPlayers()),
				startGameCommand.getTurnTimeSeconds(), startGameCommand.getGameDurationMinutes());
		gameId = startGameCommand.getGameId();
		Player[] players = startGameCommand.getPlayers();
		myPlayer = startGameCommand.getMyPlayer();
		myColor.setFill(Color.valueOf(myPlayer.getColorCode()));
		gameName.setText("Game: " + startGameCommand.getName());

		turnProperty.addListener((observable, oldValue, newValue) -> turnIndicator.setFill(
				Color.valueOf(game.getCurrentPlayer().getColorCode())));
		initStatisticsBoard(players);
		GameBoard gameBoard = startGameCommand.getGameBoard();
		CellInfo[][] matrix = gameBoard.getMatrix();
		leaveButton.setFocusTraversable(false);
		initBoard(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();
		Player firstPlayer = startGameCommand.getPlayers()[0];
		Platform.runLater(() -> turnProperty.set(firstPlayer));
		if (firstPlayer == myPlayer) {
			activeBoard.set(true);
		}

		turnTimeSeconds = startGameCommand.getTurnTimeSeconds();
		gameDurationMinutes = startGameCommand.getGameDurationMinutes();
		game.start();
		game.getStatistics().summaryPoints().toMono().subscribe(this::updateStatistics);
		initGameTimer();
		initTurnTimer();
		startTurnTimer();
		JouskaUI.onExit().subscribe(none -> leaveGame());
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

	private void initStatisticsBoard(Player[] players) {
		statisticsNodes = new EnumMap<>(Player.class);
		for (Player player : players) {
			HBox hBox = new HBox();
			hBox.setSpacing(20);

			Circle playerIcon = new Circle(16);
			playerIcon.setFill(Color.valueOf(player.getColorCode()));
			playerIcon.setStroke(Color.BLACK);

			Label pointsLabel = new Label();
			pointsLabel.setLabelFor(playerIcon);
			pointsLabel.setAlignment(Pos.TOP_CENTER);
			pointsLabel.setFont(Font.font("MV Boli", 30));

			hBox.getChildren().addAll(playerIcon, pointsLabel);

			statisticsNodes.put(player, hBox);
			statisticsBoard.getChildren().add(hBox);
		}

		game.turns().subscribe(rootChange -> {
			animateCellChanges(rootChange).blockUntilComplete();
			Platform.runLater(this::resetTurn);
		});

		game.lostPlayers().subscribe(this::handleLose);

		game.kickedPlayers().subscribe(player -> {
			animateKick(player).blockUntilComplete();
			Platform.runLater(this::resetTurn);
		});

		game.onComplete().subscribe(leftPlayers -> handleWin(leftPlayers.get(leftPlayers.size() - 1)));
	}

	private void initTurnTimer() {
		currentTurnTimer = new Timeline(new KeyFrame(Duration.seconds(1),
				ae -> turnTimeIndicator.setText(String.valueOf(Integer.parseInt(turnTimeIndicator.getText()) - 1))));

		currentTurnTimer.setCycleCount(turnTimeSeconds);
	}

	private void startTurnTimer() {
		turnTimeIndicator.setText(String.valueOf(turnTimeSeconds));
		currentTurnTimer.jumpTo(Duration.ZERO);
		currentTurnTimer.play();
	}

	private void initGameTimer() {
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
		Lock lock = game.getLock();
		try {
			lock.lock();
			activeBoard.set(false);
			game.turn(point);
			game.getStatistics().summaryPoints().toMono().subscribe(this::updateStatistics);
		}
		finally {
			lock.unlock();
		}
	}

	public void leave(Player player) {
		game.kick(player);
		game.getStatistics().summaryPoints().toMono().subscribe(this::updateStatistics);
	}

	public void handleWin(Player player) {
		activeBoard.set(false);
		turnIndicator.setFill(Color.GRAY);
		if (player == myPlayer) {
			Platform.runLater(() -> appearLoseWinLabel("You win", Color.valueOf(myPlayer.getColorCode())).play());
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

	public void handleLose(Player player) {
		turnProperty.set(game.getCurrentPlayer());
		if (player == myPlayer) {
			activeBoard.set(false);
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

	private void updateStatistics(Map<Player, Integer> playerSummaryPoints) {
		playerSummaryPoints.forEach((player, points) -> {
			Label label = (Label) statisticsNodes.get(player).getChildren().get(1);
			Platform.runLater(() -> label.setText(points + ""));
		});
	}

	private void resetTurn() {
		if (game.isCompleted()) {
			return;
		}
		turnProperty.set(game.getCurrentPlayer());
		if (game.getCurrentPlayer() == myPlayer) {
			activeBoard.set(true);
		}
		startTurnTimer();
	}

	private void initBoard(int rows, int columns) {
		gameBoard.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));
		gameBoard.disableProperty().bind(activeBoard.not());
		gameBoard.borderProperty()
				.bind(Bindings.createObjectBinding(() -> new Border(
						new BorderStroke(Color.valueOf(game.getCurrentPlayer().getColorCode()), BorderStrokeStyle.SOLID,
								CornerRadii.EMPTY, new BorderWidths(5))), turnProperty));
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
				tile.setOnMouseClicked(event -> {
					Point point = new Point(x, y);
					if (game.ownerOfPoint(point) == myPlayer) {
						activeBoard.set(false);
						IN_GAME_SERVICE.turn(gameId, point);
					}
				});
				gameBoard.getChildren().add(tile);
			}
		}
	}

	private void initTurnIndicator() {
		FadeTransition transition = new FadeTransition(Duration.seconds(1), turnIndicator);
		transition.setFromValue(0.5);
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
		Player player = cellInfo.getPlayer();
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
		JouskaUI.switchScene("menu");
	}

	public void leaveGame() {
		IN_GAME_SERVICE.leave(gameId);
	}
}
