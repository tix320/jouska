package com.github.tix320.jouska.client.ui.game;

import java.util.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.tix320.jouska.client.app.Services;
import com.github.tix320.jouska.client.ui.Controller;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Turn;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.sonder.api.common.topic.Topic;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class GameController implements Controller<StartGameCommand> {

	@FXML
	private FlowPane gameBoard;

	@FXML
	private Circle turnIndicator;

	@FXML
	private Circle myColor;

	@FXML
	private Label gameName;

	private Tile[][] tiles;

	private Topic<Turn> turnTopic;

	private Player myPlayer;

	private SimpleBooleanProperty myTurn = new SimpleBooleanProperty(false);

	private int playersCount;

	@Override
	public void initialize(StartGameCommand startGameCommand) {
		myPlayer = startGameCommand.getMyPlayer();
		this.playersCount = startGameCommand.getPlayersCount();
		Player firstTurn = startGameCommand.getFirstTurnPlayer();
		myColor.setFill(Color.valueOf(myPlayer.getColorCode()));
		gameName.setText("Game: " + startGameCommand.getName());
		if (firstTurn == myPlayer) {
			myTurn.set(true);
		}

		turnIndicator.setFill(Color.valueOf(firstTurn.getColorCode()));


		Topic<Turn> turnTopic = Services.CLONDER.registerTopic("game: " + startGameCommand.getGameId(),
				new TypeReference<>() {});
		turnTopic.asObservable()
				.subscribe(turn -> Platform.runLater(
						() -> plusToTile(turn.getI(), turn.getJ(), tiles[turn.getI()][turn.getJ()].player).subscribe(
								none -> {

									if (turn.getNextPlayer() == myPlayer) {
										myTurn.set(true);
									}

									turnIndicator.setFill(Color.valueOf(turn.getNextPlayer().getColorCode()));
								})));
		this.turnTopic = turnTopic;

		GameBoard gameBoard = startGameCommand.getGameBoard();
		CellInfo[][] matrix = gameBoard.getMatrix();
		initBoard(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();
	}

	private void initBoard(int height, int width) {
		gameBoard.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
		gameBoard.disableProperty().bind(myTurn.not());
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
				int x = i;
				int y = j;
				tile.setOnMouseClicked(event -> {
					if (tiles[x][y].player == myPlayer) {
						myTurn.set(false);
						Player nextPlayer = nextPlayer(myPlayer);
						turnTopic.publish(new Turn(x, y, myPlayer, nextPlayer))
								.subscribe(none -> Platform.runLater(
										() -> plusToTile(x, y, tiles[x][y].player).subscribe(none1 -> {
											turnIndicator.setFill(Color.valueOf(nextPlayer.getColorCode()));
											if (nextPlayer == myPlayer) {
												myTurn.set(true);
											}
										})));
					}
				});
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

	private Observable<None> plusToTile(int i, int j, Player player) {
		Publisher<None> onFinish = Publisher.simple();
		Tile tile = tiles[i][j];
		int nextPoint = tile.points + 1;
		Transition transition;
		if (nextPoint > 3) {
			transition = tile.changeContent(player, nextPoint - 4);
			transition.setOnFinished(actionEvent -> plusToNeighbors(i, j, player).subscribe(none -> {
				onFinish.publish(None.SELF);
				onFinish.complete();
			}));

		}
		else {
			transition = tile.changeContent(player, nextPoint);
			transition.setOnFinished(actionEvent -> {
				onFinish.publish(None.SELF);
				onFinish.complete();
			});
		}

		transition.play();
		return onFinish.asObservable();
	}

	Set<Point> nextGenSet = new HashSet<>();

	private Observable<None> plusToNeighbors(int i, int j, Player player) {
		Publisher<None> onFinish = Publisher.simple();
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
				Publisher<Point> publisher = Publisher.simple();
				publisher.asObservable().subscribe(point -> {
					Tile tile = tiles[point.i][point.j];
					tile.points -= 1;
					plusToTile(point.i, point.j, player).subscribe(none -> {
						if (!nextGen.isEmpty()) {
							Point nextPoint = nextGen.poll();
							publisher.publish(nextPoint);
							nextGenSet.remove(nextPoint);
						}
						else {
							publisher.complete();
							onFinish.publish(None.SELF);
							onFinish.complete();
						}
					});
				});
				Point point = nextGen.poll();
				publisher.publish(point);
				nextGenSet.remove(point);
			});
		}
		else {
			parallelTransition.setOnFinished(actionEvent -> {
				onFinish.publish(None.SELF);
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
