package com.github.tix320.jouska.client.ui.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.service.origin.ClientGameManagementOrigin;
import com.github.tix320.jouska.client.service.origin.ClientGameOrigin;
import com.github.tix320.jouska.client.ui.game.PlayerMode;
import com.github.tix320.jouska.client.ui.game.Tile;
import com.github.tix320.jouska.client.ui.helper.component.SpeedSlider;
import com.github.tix320.jouska.client.ui.helper.component.TimerLabel;
import com.github.tix320.jouska.core.application.game.*;
import com.github.tix320.jouska.core.application.game.creation.GameBoards;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.SimpleGameSettings;
import com.github.tix320.jouska.core.application.game.creation.TimedGameSettings;
import com.github.tix320.jouska.core.dto.*;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.publisher.MonoPublisher;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.skimp.api.object.None;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import static java.util.stream.Collectors.toMap;

public class GameController implements Controller<GameWatchDto> {

	@FXML
	private AnchorPane mainPane;

	@FXML
	private GridPane gameBoardPane;

	@FXML
	private Circle turnIndicator;

	@FXML
	private TimerLabel turnTimeIndicator;

	@FXML
	private TimerLabel turnTotalTimeIndicator;

	@FXML
	private Label gameNameLabel;

	@FXML
	private AnchorPane rightPane;

	@FXML
	private VBox statisticsBoard;

	@FXML
	private Label loseWinLabel;

	@FXML
	private SpeedSlider gameSpeedSlider;

	private PlayerMode playerMode;

	private Map<PlayerColor, Long> playerTurnRemainingMillis;

	private Tile[][] tiles;
	private Map<PlayerColor, Parent> statisticsNodes;

	private String gameId;
	private Game game;
	private TimedGameSettings gameSettings;
	private GamePlayer myPlayer;

	private final SimpleObjectProperty<GamePlayer> turnProperty = new SimpleObjectProperty<>();

	private final AtomicBoolean turned = new AtomicBoolean();

	private final MonoPublisher<None> destroyPublisher = Publisher.mono();

	private final SimpleDoubleProperty gameSpeedCoefficient = new SimpleDoubleProperty(1);

	private final ClientGameOrigin gameOrigin;

	private final ClientGameManagementOrigin gameManagementOrigin;

	public GameController(ClientGameOrigin gameOrigin, ClientGameManagementOrigin gameManagementOrigin) {
		this.gameOrigin = gameOrigin;
		this.gameManagementOrigin = gameManagementOrigin;
	}

	@Override
	public void init(GameWatchDto gameWatchDto) {
		if (gameWatchDto instanceof GamePlayDto) {
			this.playerMode = PlayerMode.PLAY;
			rightPane.getChildren().remove(gameSpeedSlider);
		} else {
			this.playerMode = PlayerMode.WATCH;
			Platform.runLater(() -> {
				gameSpeedSlider.setLabel("Game speed");
				gameSpeedSlider.setMinValue(0.1);
				gameSpeedSlider.setMaxValue(10);
				gameSpeedCoefficient.bind(gameSpeedSlider.speedCoefficientProperty());
				gameSpeedSlider.setValue(1);
			});
		}


		this.gameId = gameWatchDto.getGameId();
		GameSettings gameSettings = gameWatchDto.getGameSettings().toModel();
		if (!(gameSettings instanceof TimedGameSettings)) {
			throw new UnsupportedOperationException("Not implemented for non timed games");
		}
		this.gameSettings = (TimedGameSettings) gameSettings;

		List<GamePlayer> players = gameWatchDto.getPlayers();
		GameBoard gameBoard = GameBoards.createByType(this.gameSettings.getBoardType(),
				players.stream().map(GamePlayer::getColor).collect(Collectors.toList()));

		this.game = SimpleGame.create((SimpleGameSettings) this.gameSettings.getWrappedGameSettings());
		players.forEach(gamePlayer -> game.addPlayer(gamePlayer));

		if (playerMode == PlayerMode.PLAY) {
			this.myPlayer = new GamePlayer(CurrentUserContext.getPlayer(), ((GamePlayDto) gameWatchDto).getMyPlayer());
		} else {
			this.myPlayer = new GamePlayer(CurrentUserContext.getPlayer(), PlayerColor.RED);
		}

		this.gameNameLabel.setText("Game: " + this.gameSettings.getName());

		createStatisticsBoardComponent(players);
		BoardCell[][] matrix = gameBoard.getMatrix();
		createBoardComponent(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();

		DateTimeFormatter timersFormatter = DateTimeFormatter.ofPattern("mm:ss");
		turnTimeIndicator.setFormatter(timersFormatter);
		turnTotalTimeIndicator.setFormatter(timersFormatter);

		initTotalTurnTimers(players);

		registerListeners();

		game.start();

		GamePlayer firstPlayer = players.get(0);
		turnProperty.set(firstPlayer);
		startTurnTimer();
		Platform.runLater(() -> {
			turnTotalTimeIndicator.setTime(
					LocalTime.ofSecondOfDay(this.gameSettings.getPlayerTurnTotalDurationSeconds()));
			turnTotalTimeIndicator.run(gameSpeedCoefficient.get());
		});

		updateStatistics(game.getStatistics().summaryPoints());
		turned.set(false);
	}

	private void registerListeners() {
		MonoObservable<?> destroy = destroyPublisher.asObservable();

		game.completed().takeUntil(destroy).subscribe(none -> onGameComplete());

		destroy.subscribeOnComplete(completion -> Platform.runLater(() -> {
			turnTimeIndicator.stop();
			turnTotalTimeIndicator.stop();
		}));

		gameOrigin.changes(gameId).takeUntil(destroy).subscribe(this::processChange);
	}

	private void processChange(GameChangeDto gameChange) {
		if (gameChange instanceof PlayerTimedTurnDto) {
			PlayerTimedTurnDto playerTurn = (PlayerTimedTurnDto) gameChange;
			turn(playerTurn);
			List<GamePlayer> losers = game.getLosers();
			if (losers.contains(myPlayer)) {
				showLose();
			}
		} else if (gameChange instanceof PlayerLeaveDto) {
			PlayerLeaveDto playerLeave = (PlayerLeaveDto) gameChange;
			kickPlayer(playerLeave);
		} else if (gameChange instanceof GameCompleteDto) {
			GameCompleteDto gameComplete = (GameCompleteDto) gameChange;
			if (!game.isCompleted()) {
				game.forceCompleteGame(gameComplete.getWinner().getRealPlayer());
			}
		} else {
			throw new IllegalArgumentException();
		}
		turned.set(false);
	}

	@Override
	public void destroy() {
		destroyPublisher.complete();
	}

	@FXML
	private void onFullScreenClick() {
		Stage stage = UI.stage;
		stage.setFullScreen(!stage.isFullScreen());
	}

	private void createStatisticsBoardComponent(List<GamePlayer> players) {
		statisticsNodes = new EnumMap<>(PlayerColor.class);
		for (GamePlayer player : players) {
			GridPane pane = new GridPane();

			Circle playerIcon = new Circle(16);
			playerIcon.setFill(Color.valueOf(player.getColor().getColorCode()));
			playerIcon.setStroke(Color.BLACK);

			Label nicknameLabel = new Label();
			nicknameLabel.setTextFill(Color.web(player.getColor().getColorCode()));
			nicknameLabel.setAlignment(Pos.CENTER);
			nicknameLabel.setFont(Font.font(25));
			nicknameLabel.setText(player.getRealPlayer().getNickname());
			if (player.equals(myPlayer)) {
				nicknameLabel.setBorder(new Border(
						new BorderStroke(Color.web("#948fff"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
								BorderWidths.DEFAULT)));
			}

			Label pointsLabel = new Label();
			pointsLabel.setTextFill(Color.web(player.getColor().getColorCode()));
			pointsLabel.setAlignment(Pos.CENTER);
			pointsLabel.setFont(Font.font(25));

			pane.add(playerIcon, 0, 0);
			pane.add(pointsLabel, 1, 0);
			pane.add(nicknameLabel, 2, 0);
			ColumnConstraints constraints = new ColumnConstraints();
			constraints.setPercentWidth(20);
			pane.getColumnConstraints().add(constraints);
			constraints = new ColumnConstraints();
			constraints.setPercentWidth(25);
			pane.getColumnConstraints().add(constraints);
			constraints = new ColumnConstraints();
			constraints.setPercentWidth(65);
			pane.getColumnConstraints().add(constraints);

			statisticsNodes.put(player.getColor(), pane);
			statisticsBoard.getChildren().add(pane);
		}
	}

	private void initTotalTurnTimers(List<GamePlayer> players) {
		playerTurnRemainingMillis = players.stream()
				.map(GamePlayer::getColor)
				.collect(toMap(color -> color, color -> gameSettings.getPlayerTurnTotalDurationSeconds() * 1000L));
	}

	private void startTurnTimer() {
		Platform.runLater(() -> {
			int turnDurationSeconds = gameSettings.getTurnDurationSeconds();
			turnTimeIndicator.setTime(LocalTime.ofSecondOfDay(turnDurationSeconds));
			turnTimeIndicator.run(gameSpeedCoefficient.get());
		});
	}

	public void turn(PlayerTimedTurnDto playerTurnDto) {
		Point point = playerTurnDto.getPoint();
		GamePlayer previousPlayer = game.getCurrentPlayer();
		CellChange rootChange = game.turn(point);
		if (playerMode == PlayerMode.WATCH) {
			long turnDurationMillis = gameSettings.getTurnDurationSeconds() * 1000L;
			long remainingTurnMillis = playerTurnDto.getRemainingTurnMillis();
			long timeToSleep = (long) ((turnDurationMillis - remainingTurnMillis) / gameSpeedCoefficient.get());
			try {
				Thread.sleep(timeToSleep);
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
		Platform.runLater(() -> {
			turnTimeIndicator.stop();
			turnTotalTimeIndicator.stop();
		});
		animateCellChanges(rootChange);
		Map<GamePlayer, Integer> statistics = game.getStatistics().summaryPoints();
		updateStatistics(statistics);

		if (game.isCompleted()) {
			return;
		}

		GamePlayer currentPlayer = game.getCurrentPlayer();
		Platform.runLater(() -> {
			turnProperty.set(currentPlayer);

			if (destroyPublisher.isCompleted()) {
				return;
			}

			playerTurnRemainingMillis.put(previousPlayer.getColor(), playerTurnDto.getRemainingPlayerTotalTurnMillis());

			long currentPlayerRemainingMillis = playerTurnRemainingMillis.get(currentPlayer.getColor());

			turnTotalTimeIndicator.setTime(
					LocalTime.ofSecondOfDay(TimeUnit.MILLISECONDS.toSeconds(currentPlayerRemainingMillis)));
			turnTotalTimeIndicator.run(gameSpeedCoefficient.get());

			int turnDurationSeconds = gameSettings.getTurnDurationSeconds();
			turnTimeIndicator.setTime(LocalTime.ofSecondOfDay(turnDurationSeconds));
			turnTimeIndicator.run(gameSpeedCoefficient.get());
		});
	}

	public void kickPlayer(PlayerLeaveDto playerLeaveDto) {
		Player player = playerLeaveDto.getPlayer();
		PlayerWithPoints playerWithPoints = game.kick(player);
		animateLeave(playerWithPoints);
		Map<GamePlayer, Integer> statistics = game.getStatistics().summaryPoints();
		updateStatistics(statistics);
	}

	private void onGameComplete() {
		GamePlayer winner = game.getWinner().orElseThrow();

		resetTimersToZero();
		Platform.runLater(() -> {
			if (playerMode == PlayerMode.WATCH) {
				show3PartyWin(winner);
			} else {
				if (myPlayer.equals(winner)) {
					showWin();
				}
			}
		});
	}

	private void resetTimersToZero() {
		Platform.runLater(() -> {
			turnTimeIndicator.stop();
			turnTotalTimeIndicator.stop();
			turnTimeIndicator.setTime(LocalTime.MIDNIGHT);
			turnTimeIndicator.setTextFill(Color.RED);
			turnTotalTimeIndicator.setTime(LocalTime.MIDNIGHT);
			turnTotalTimeIndicator.setTextFill(Color.RED);
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

	public void show3PartyWin(GamePlayer winner) {
		Platform.runLater(() -> {
			Transition transition = appearLoseWinLabel(winner.getRealPlayer().getNickname() + " wins",
					Color.valueOf(winner.getColor().getColorCode()));
			transition.play();
		});
	}

	public void animateLeave(PlayerWithPoints playerWithPoints) {
		Duration animationDuration = calculateAnimationDuration();
		List<Point> pointsBelongedToPlayer = playerWithPoints.getPoints();
		Transition[] transitions = pointsBelongedToPlayer.stream()
				.map(point -> makeTileTransition(point, new BoardCell(null, 0), animationDuration))
				.toArray(Transition[]::new);

		ParallelTransition fullTransition = new ParallelTransition(transitions);
		Publisher<None> onFinishPublisher = Publisher.mono();
		fullTransition.setOnFinished(event -> onFinishPublisher.publish(None.SELF));
		fullTransition.play();

		onFinishPublisher.asObservable().await(java.time.Duration.ofMinutes(3));
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

	private void updateStatistics(Map<GamePlayer, Integer> playerSummaryPoints) {
		playerSummaryPoints.forEach((player, points) -> {
			Label label = (Label) statisticsNodes.get(player.getColor()).getChildrenUnmodifiable().get(1);
			Platform.runLater(() -> label.setText(points + ""));
		});
	}

	private void createBoardComponent(int rows, int columns) {
		gameBoardPane.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5))));
		turnProperty.addListener((observable, oldValue, newValue) -> {
			Border border = new Border(new BorderStroke(Color.valueOf(turnProperty.get().getColor().getColorCode()),
					BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(5)));
			gameBoardPane.setBorder(border);
		});
		tiles = new Tile[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				Tile tile = new Tile();
				tiles[i][j] = tile;
				int x = i;
				int y = j;
				tile.setHoverColorFactory(() -> {
					if (game.isCompleted()) {
						return null;
					} else {
						PlayerColor currentPlayerColor = game.getCurrentPlayer().getColor();
						PlayerColor myPlayerColor = myPlayer.getColor();
						if (currentPlayerColor == myPlayerColor
							&& game.getBoard().get(x, y).getColor() == myPlayerColor) {
							return Color.web(currentPlayerColor.getColorCode());
						} else {
							return Color.rgb(181, 167, 167);
						}
					}
				});
				tile.setOnMouseClicked(event -> onTileClick(x, y, tile));
				gameBoardPane.add(tile, j, i);
			}
		}

		double rowPercentPerTile = 100.0 / rows;
		double columnPercentPerTile = 100.0 / columns;

		List<RowConstraints> rowConstraints = IntStream.range(0, rows).mapToObj(operand -> {
			RowConstraints constraints = new RowConstraints();
			constraints.setPercentHeight(rowPercentPerTile);
			return constraints;
		}).collect(Collectors.toList());

		List<ColumnConstraints> columnConstraints = IntStream.range(0, rows).mapToObj(operand -> {
			ColumnConstraints constraints = new ColumnConstraints();
			constraints.setPercentWidth(columnPercentPerTile);
			return constraints;
		}).collect(Collectors.toList());

		gameBoardPane.getRowConstraints().addAll(rowConstraints);
		gameBoardPane.getColumnConstraints().addAll(columnConstraints);

		mainPane.setPrefWidth(gameBoardPane.getWidth() + 400);
		mainPane.setPrefHeight(gameBoardPane.getHeight() + 200);
	}

	private void initTurnIndicator() {
		turnProperty.addListener((observable, oldValue, newValue) -> turnIndicator.setFill(
				Color.valueOf(newValue.getColor().getColorCode())));

		FadeTransition transition = new FadeTransition(Duration.seconds(0.6), turnIndicator);
		transition.setFromValue(0.3);
		transition.setToValue(1);
		transition.setCycleCount(Timeline.INDEFINITE);
		transition.setAutoReverse(true);
		transition.play();
		destroyPublisher.asObservable().subscribeOnComplete(completion -> transition.stop());
	}

	private void fillBoard(BoardCell[][] board) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				BoardCell boardCell = board[i][j];
				if (boardCell.getColor() != null) {
					tiles[i][j].makeAppearTransition(boardCell.getColor(), boardCell.getPoints(),
							Duration.seconds(Constants.GAME_BOARD_TILE_ANIMATION_SECONDS)).play();
				}
			}
		}
	}

	private void onTileClick(int x, int y, Tile tile) {
		if (game.isCompleted() || !turnProperty.get().equals(myPlayer) || !turned.compareAndSet(false, true)) {
			return;
		}

		Point point = new Point(x, y);
		game.ownerOfPoint(point).ifPresentOrElse(player -> {
			if (myPlayer.equals(player)) {
				gameOrigin.turn(gameId, point);
				turned.set(true);
				tile.animateBackground(Color.web(myPlayer.getColor().getColorCode())).play();
			} else {
				turned.set(false);
				tile.animateBackground(Color.GRAY).play();
			}
		}, () -> turned.set(false));
	}

	private void animateCellChanges(CellChange root) {
		Duration animationDuration = calculateAnimationDuration();

		SequentialTransition fullAnimation = new SequentialTransition();

		Publisher<None> onFinishPublisher = Publisher.mono();
		fullAnimation.setOnFinished(event -> onFinishPublisher.publish(None.SELF));

		fullAnimation.getChildren().add(makeTileTransition(root.getPoint(), root.getBoardCell(), animationDuration));

		if (root.getChildren().isEmpty()) {
			fullAnimation.play();

			onFinishPublisher.asObservable().await(java.time.Duration.ofMinutes(3));
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
				transition.getChildren()
						.add(makeTileTransition(cellChange.getPoint(), cellChange.getBoardCell(), animationDuration));
			}

			ParallelTransition childrenTransition = new ParallelTransition();
			for (CellChange child : cellChange.getChildren()) {
				Transition childTransition = makeTileTransition(child.getPoint(), child.getBoardCell(),
						animationDuration);
				childrenTransition.getChildren().add(childTransition);
			}
			transition.getChildren().add(childrenTransition);

			fullAnimation.getChildren().add(transition);
		}

		fullAnimation.play();
		onFinishPublisher.asObservable().await(java.time.Duration.ofMinutes(3));
	}

	private Transition makeTileTransition(Point point, BoardCell boardCell, Duration duration) {
		int i = point.i;
		int j = point.j;
		Tile tile = tiles[i][j];
		int points = boardCell.getPoints();
		PlayerColor player = boardCell.getColor();
		switch (points) {
			case 0:
				return tile.makeDisappearTransition(duration);
			case 1:
				return tile.makeAppearTransition(player, points, duration);
			case 2:
			case 3:
			case 4:
				return tile.makeDisAppearAndAppearTransition(player, points, duration);
			default:
				throw new IllegalStateException(points + "");
		}
	}

	private Duration calculateAnimationDuration() {
		double animationSeconds = Constants.GAME_BOARD_TILE_ANIMATION_SECONDS / gameSpeedCoefficient.get();
		return Duration.seconds(animationSeconds);
	}

	public void onLeaveClick() {
		destroyPublisher.complete();
		if (!game.isCompleted() && playerMode == PlayerMode.PLAY) {
			gameManagementOrigin.leave(gameId);
		}
		UI.switchComponent(MenuController.class);
	}
}
