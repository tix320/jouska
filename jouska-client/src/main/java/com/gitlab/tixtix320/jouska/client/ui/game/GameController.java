package com.gitlab.tixtix320.jouska.client.ui.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gitlab.tixtix320.jouska.client.ui.Controller;
import com.gitlab.tixtix320.jouska.core.model.CellInfo;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.Player;
import com.gitlab.tixtix320.jouska.core.model.Turn;
import com.gitlab.tixtix320.sonder.api.common.topic.Topic;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class GameController implements Controller<Map<String, Object>> {

	@FXML
	private AnchorPane root;

	@FXML
	private FlowPane gameBoard;

	@FXML
	private Circle turnIndicator;

	private Tile[][] tiles;

	private Topic<Turn> turnTopic;

	private Player you;

	private SimpleBooleanProperty yourTurn = new SimpleBooleanProperty(false);

	private int playersCount;

	@Override
	public void initialize(Map<String, Object> data) {
		gameBoard.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
		gameBoard.disableProperty().bind(yourTurn.not());
		GameBoard gameBoard = (GameBoard) data.get("board");
		you = (Player) data.get("player");
		Player firstTurn = (Player) data.get("firstTurn");
		if (firstTurn == you) {
			yourTurn.set(true);
		}

		this.playersCount = (int) data.get("playersCount");

		@SuppressWarnings("unchecked")
		Topic<Turn> turnTopic = (Topic<Turn>) data.get("turnTopic");
		turnTopic.asObservable().subscribe(turn -> Platform.runLater(() -> {
			new Timeline(plusPointToTile(turn.getX(), turn.getY(), tiles[turn.getX()][turn.getY()].color).toArray(
					KeyFrame[]::new)).play();

			if (turn.getNextPlayer() == you) {
				yourTurn.set(true);
			}

		}));
		this.turnTopic = turnTopic;

		CellInfo[][] matrix = gameBoard.getMatrix();
		initBoard(matrix.length, matrix[0].length);
		fillBoard(matrix);
	}

	private void initBoard(int height, int width) {
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
					if (tiles[x][y].color != Player.NONE && tiles[x][y].color == you) {
						yourTurn.set(false);
						turnTopic.publish(new Turn(x, y, you, nextPlayer(you)))
								.subscribe(none -> Platform.runLater(() -> new Timeline(
										plusPointToTile(x, y, tiles[x][y].color).toArray(KeyFrame[]::new)).play()));
					}
				});
				gameBoard.getChildren().add(tile);
			}
		}
	}

	private void fillBoard(CellInfo[][] board) {
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				CellInfo cellInfo = board[i][j];
				Tile tile = tiles[i][j];
				new Timeline(tile.changeContent(Player.fromNumber(cellInfo.getColor()), cellInfo.getPoints())
						.toArray(KeyFrame[]::new)).play();
			}
		}
	}

	private List<KeyFrame> plusPointToTile(int i, int j, Player player) {
		Tile tile = tiles[i][j];
		if (tile.points == 3) {
			List<KeyFrame> keyFrames = new ArrayList<>(tile.changeContent(Player.NONE, 0));
			if (i - 1 >= 0) {
				keyFrames.addAll(plusPointToTile(i - 1, j, player));
			}
			if (i + 1 < tiles.length) {
				keyFrames.addAll(plusPointToTile(i + 1, j, player));
			}
			if (j - 1 >= 0) {
				keyFrames.addAll(plusPointToTile(i, j - 1, player));
			}
			if (j + 1 < tiles[0].length) {
				keyFrames.addAll(plusPointToTile(i, j + 1, player));
			}
			return keyFrames;
		}
		else {
			return tile.changeContent(player, tile.points + 1);
		}
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
