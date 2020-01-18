package com.github.tix320.jouska.client.ui.game;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tix320.jouska.client.app.Services;
import com.github.tix320.jouska.client.ui.Controller;
import com.github.tix320.jouska.core.dto.WatchGameCommand;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Turn;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.subject.Subject;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class GameWatchingController implements Controller<WatchGameCommand> {

	@FXML
	private FlowPane gameBoard;

	@FXML
	private Circle turnIndicator;

	@FXML
	private Label gameName;

	private Tile[][] tiles;

	private int playersCount;

	@Override
	public void initialize(WatchGameCommand watchGameCommand) {
		this.playersCount = watchGameCommand.getPlayersCount();
		Player firstTurn = watchGameCommand.getFirstTurnPlayer();

		gameName.setText("Game: " + watchGameCommand.getName());
		turnIndicator.setFill(Color.valueOf(firstTurn.getColorCode()));

		Observable<Turn> turns = Services.CLONDER.registerTopic("game: " + watchGameCommand.getGameId(),
				new TypeReference<Turn>() {}, 10).asObservable();


		GameBoard gameBoard = watchGameCommand.getInitialGameBoard();
		CellInfo[][] matrix = gameBoard.getMatrix();
		initBoard(matrix.length, matrix[0].length);
		applyTurns(watchGameCommand.getTurns(), matrix);
		fillBoard(matrix);
		initTurnIndicator();
		turns.subscribe(turn -> Platform.runLater(
				() -> plusToTile(turn.getI(), turn.getJ(), tiles[turn.getI()][turn.getJ()].player).subscribe(
						none -> turnIndicator.setFill(Color.valueOf(turn.getNextPlayer().getColorCode())))));
	}

	private void initBoard(int height, int width) {
		gameBoard.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
		gameBoard.setPrefWidth(width * 100 + 10);
		gameBoard.setMaxWidth(width * 100 + 10);
		tiles = new Tile[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Tile tile = new Tile();
				tile.setPrefWidth(100);
				tile.setPrefHeight(100);
				tile.setLayoutX(i * 100);
				tile.setLayoutY(j * 100);
				tile.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, CornerRadii.EMPTY,
						BorderWidths.DEFAULT)));
				tiles[i][j] = tile;
				gameBoard.getChildren().add(tile);
			}
		}
	}

	private void initTurnIndicator() {
		FadeTransition transition = new FadeTransition(Duration.seconds(1), turnIndicator);
		transition.setFromValue(0);
		transition.setToValue(1);
		transition.setCycleCount(Timeline.INDEFINITE);
		transition.setAutoReverse(true);
		transition.play();
	}

	private void fillBoard(CellInfo[][] board) {
		ParallelTransition transition = new ParallelTransition();
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				CellInfo cellInfo = board[i][j];
				Tile tile = tiles[i][j];
				transition.getChildren()
						.add(tile.changeContent(Player.fromNumber(cellInfo.getColor()), cellInfo.getPoints()));
			}
		}
		transition.play();
	}

	private void applyTurns(List<Turn> turns, CellInfo[][] matrix) {
		for (Turn turn : turns) {
			int i = turn.getI();
			int j = turn.getJ();
			Player player = turn.getCurrentPlayer();

			turn(matrix, i, j, player);
		}
	}

	private void turn(CellInfo[][] matrix, int i, int j, Player player) {
		CellInfo cellInfo = matrix[i][j];
		int cellPoints = cellInfo.getPoints();
		int nextPoint = cellPoints + 1;
		if (nextPoint > 3) {
			matrix[i][j] = new CellInfo(player.ordinal(), nextPoint - 4);
			;
			List<Point> neighbors = findNeighbors(new Point(i, j));
			for (Point point : neighbors) {
				turn(matrix, point.i, point.j, player);
			}
		}
		else {
			matrix[i][j] = new CellInfo(player.ordinal(), cellPoints + 1);
		}
	}

	private Observable<None> plusToTile(int i, int j, Player player) {
		Subject<None> onFinish = Subject.single();
		Tile tile = tiles[i][j];
		int nextPoint = tile.points + 1;
		Transition transition;
		if (nextPoint > 3) {
			transition = tile.changeContent(player, nextPoint - 4);
			transition.setOnFinished(actionEvent -> plusToNeighbors(i, j, player).subscribe(none -> {
				onFinish.next(None.SELF);
				onFinish.complete();
			}));

		}
		else {
			transition = tile.changeContent(player, nextPoint);
			transition.setOnFinished(actionEvent -> {
				onFinish.next(None.SELF);
				onFinish.complete();
			});
		}

		transition.play();
		return onFinish.asObservable();
	}

	Set<Point> nextGenSet = new HashSet<>();

	private Observable<None> plusToNeighbors(int i, int j, Player player) {
		Subject<None> onFinish = Subject.single();
		ParallelTransition parallelTransition = new ParallelTransition();
		List<Point> neighbors = findNeighbors(new Point(i, j));
		Queue<Point> nextGen = new LinkedList<>();
		for (Point point : neighbors) {
			Tile tile = tiles[point.i][point.j];
			int nextTilePoint = tile.points + 1;
			parallelTransition.getChildren().add(tile.changeContent(player, nextTilePoint));
			if (nextTilePoint > 3) {
				if (!nextGenSet.contains(point)) {
					nextGen.add(point);
					nextGenSet.add(point);
				}
			}
		}
		if (!nextGen.isEmpty()) {
			parallelTransition.setOnFinished(actionEvent -> {
				Subject<Point> subject = Subject.single();
				subject.asObservable().subscribe(point -> {
					Tile tile = tiles[point.i][point.j];
					tile.points -= 1;
					plusToTile(point.i, point.j, player).subscribe(none -> {
						if (!nextGen.isEmpty()) {
							Point nextPoint = nextGen.poll();
							subject.next(nextPoint);
							nextGenSet.remove(nextPoint);
						}
						else {
							subject.complete();
							onFinish.next(None.SELF);
							onFinish.complete();
						}
					});
				});
				Point point = nextGen.poll();
				subject.next(point);
				nextGenSet.remove(point);
			});
		}
		else {
			parallelTransition.setOnFinished(actionEvent -> {
				onFinish.next(None.SELF);
				onFinish.complete();
			});
		}
		parallelTransition.play();
		return onFinish.asObservable();
	}

	private List<Point> findNeighbors(Point point) {
		int i = point.i;
		int j = point.j;
		List<Point> points = new ArrayList<>(4);
		if (i - 1 >= 0) {
			points.add(new Point(i - 1, j));
		}
		if (i + 1 < tiles.length) {
			points.add(new Point(i + 1, j));
		}
		if (j - 1 >= 0) {
			points.add(new Point(i, j - 1));
		}
		if (j + 1 < tiles[0].length) {
			points.add(new Point(i, j + 1));
		}
		return points;
	}

	private Player nextPlayer(Player currentPlayer) {
		if (currentPlayer.ordinal() == playersCount) {
			return Player.fromNumber(1);
		}
		else {
			return Player.fromNumber(currentPlayer.ordinal() + 1);
		}
	}
}
