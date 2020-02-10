package com.github.tix320.jouska.client.ui.game;

import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

import com.github.tix320.jouska.client.app.Jouska;
import com.github.tix320.jouska.client.ui.Controller;
import com.github.tix320.jouska.core.dto.StartGameCommand;
import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.jouska.core.game.JouskaGame.CellChange;
import com.github.tix320.jouska.core.model.CellInfo;
import com.github.tix320.jouska.core.model.GameBoard;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Point;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
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
	private Circle myColor;

	@FXML
	private Label gameName;

	@FXML
	private VBox statisticsBoard;

	@FXML
	private Label loseWinLabel;

	@FXML
	private Button leaveButton;

	private Map<Player, HBox> statisticsNodes;

	private Tile[][] tiles;

	private long gameId;

	private Player myPlayer;

	private SimpleObjectProperty<Player> turnProperty = new SimpleObjectProperty<>();

	private SimpleBooleanProperty activeBoard = new SimpleBooleanProperty();

	private JouskaGame game;

	@Override
	public void initialize(StartGameCommand startGameCommand) {
		CURRENT = this;
		leaveButton.setFocusTraversable(false);
		game = new JouskaGame(startGameCommand.getGameBoard(), startGameCommand.getPlayers());
		gameId = startGameCommand.getGameId();
		Player[] players = startGameCommand.getPlayers();
		myPlayer = startGameCommand.getMyPlayer();
		myColor.setFill(Color.valueOf(myPlayer.getColorCode()));
		gameName.setText("Game: " + startGameCommand.getName());

		// ObjectBinding objectBinding = Bindings.createObjectBinding(() -> {
		// 	System.out.println("poxvav: " + game.getCurrentPlayer());
		// 	return Color.valueOf(game.getCurrentPlayer().getColorCode());
		// }, turnProperty);
		// turnIndicator.fillProperty().bind(objectBinding);

		turnProperty.addListener(new ChangeListener<Player>() {
			@Override
			public void changed(ObservableValue<? extends Player> observable, Player oldValue, Player newValue) {
				turnIndicator.setFill(Color.valueOf(game.getCurrentPlayer().getColorCode()));
			}
		});
		initStatisticsBoard(players);
		GameBoard gameBoard = startGameCommand.getGameBoard();
		CellInfo[][] matrix = gameBoard.getMatrix();
		initBoard(matrix.length, matrix[0].length);
		fillBoard(matrix);
		initTurnIndicator();
		Player firstPlayer = startGameCommand.getPlayers()[0];
		Platform.runLater(() -> {
			turnProperty.set(firstPlayer);

		});
		if (firstPlayer == myPlayer) {
			activeBoard.set(true);
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
		updateStatistics();
	}

	public void turn(Point point) {
		CellChange cellChange = game.turn(point);

		animateCellChanges(cellChange).waitComplete().subscribe(none -> {
			Platform.runLater(() -> {
				turnProperty.set(game.getCurrentPlayer());
				updateStatistics();
				if (game.getCurrentPlayer() == myPlayer) {
					activeBoard.set(true);
				}
			});
		});
	}

	public void win(Player player) {
		if (player == myPlayer) {
			Platform.runLater(() -> {
				activeBoard.set(false);
				turnIndicator.setFill(Color.GRAY);
				appearLoseWinLabel("You win", Color.SPRINGGREEN).play();
			});
		}
	}

	public void lose(Player player) {
		turnProperty.set(game.getCurrentPlayer());
		Platform.runLater(() -> {
			HBox statisticsNode = statisticsNodes.remove(player);
			Transition transition = disappearStatisticsNode(statisticsNode);
			if (player == myPlayer) {
				activeBoard.set(false);
				transition = new ParallelTransition(transition, appearLoseWinLabel("You lose", Color.RED));
			}

			transition.play();
		});
	}

	private Transition disappearStatisticsNode(Node node) {
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(1), node);
		translateTransition.setFromX(0);
		translateTransition.setToX(200);
		FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), node);
		fadeTransition.setFromValue(1);
		fadeTransition.setToValue(0);
		ParallelTransition parallelTransition = new ParallelTransition(translateTransition, fadeTransition);
		parallelTransition.setOnFinished(event -> mainPane.getChildren().remove(node));
		return parallelTransition;
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

	private void updateStatistics() {
		Map<Player, Integer> playerSummaryPoints = game.getPlayerSummaryPoints();
		playerSummaryPoints.forEach((player, points) -> {
			Label label = (Label) statisticsNodes.get(player).getChildren().get(1);
			label.setText(points + "");
		});

	}

	private void initBoard(int height, int width) {
		gameBoard.setBorder(new Border(
				new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
		gameBoard.disableProperty().bind(activeBoard.not());
		gameBoard.borderProperty()
				.bind(Bindings.createObjectBinding(() -> new Border(
						new BorderStroke(Color.valueOf(game.getCurrentPlayer().getColorCode()), BorderStrokeStyle.SOLID,
								CornerRadii.EMPTY, new BorderWidths(5))), turnProperty));
		// gameBoard.setPrefWidth(width * 100 + 10);
		// gameBoard.setMaxWidth(width * 100 + 10);
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
					Point point = new Point(x, y);
					if (game.playerOf(point) == myPlayer) {
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


	public void leaveGame(ActionEvent actionEvent) {
		Jouska.switchScene("game-joining");
	}
}
