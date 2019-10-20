package com.gitlab.tixtix320.jouska.client.ui;

import com.gitlab.tixtix320.jouska.core.model.CellInfo;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.Turn;
import com.gitlab.tixtix320.sonder.api.common.topic.Topic;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;

import java.util.Map;

public class GameController implements Controller<Map<String, Object>> {

    private static final String[][] jouskas = new String[][]{
            {"ui/game/blue-jouska-1.png", "ui/game/blue-jouska-1.png", "ui/game/blue-jouska-2.png", "ui/game/blue-jouska-3.png", "ui/game/blue-jouska-4.png"},
            {"ui/game/blue-jouska-1.png", "ui/game/green-jouska-1.png", "ui/game/green-jouska-2.png", "ui/game/green-jouska-3.png", "ui/game/green-jouska-4.png"},
            {"ui/game/blue-jouska-1.png", "ui/game/red-jouska-1.png", "ui/game/red-jouska-2.png", "ui/game/red-jouska-3.png", "ui/game/red-jouska-4.png"},
            {"ui/game/blue-jouska-1.png", "ui/game/yellow-jouska-1.png", "ui/game/yellow-jouska-2.png", "ui/game/yellow-jouska-3.png", "ui/game/yellow-jouska-4.png"},
    };

    @FXML
    private AnchorPane root;

    @FXML
    private GridPane gameGrid;

    @FXML
    private Label timeIndicator;

    @FXML
    private Circle turnIndicator;

    private Cell[][] cells;

    private Topic<Turn> turnTopic;

    @Override
    public void initialize(Map<String, Object> data) {
        GameBoard gameBoard = (GameBoard) data.get("board");

        @SuppressWarnings("unchecked")
        Topic<Turn> turnTopic = (Topic<Turn>) data.get("turnTopic");
        turnTopic.asObservable().subscribe(turn -> Platform.runLater(() -> pointToCell(turn.getX(), turn.getY(), null)));
        this.turnTopic = turnTopic;

        CellInfo[][] matrix = gameBoard.getMatrix();
        initBoard(matrix.length, matrix[0].length);
        fillBoard(matrix);
    }

    private void initBoard(int height, int width) {
        cells = new Cell[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Group wrapper = new Group();
                ImageView node = new ImageView(jouskas[0][0]);
                wrapper.getChildren().add(node);
                int x = i;
                int y = j;
                wrapper.setOnMouseClicked(event -> {
                    if (cells[x][y].color != 0) {
                        if (true) { // my turn
                            turnTopic.publish(new Turn(x, y)).subscribe(none -> Platform.runLater(() -> pointToCell(x, y, null)));
                        }
                    }
                });
                cells[i][j] = new Cell(wrapper);
                gameGrid.add(wrapper, j, i);
            }
        }
    }

    private void fillBoard(CellInfo[][] board) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                CellInfo cellInfo = board[i][j];
                Cell cell = cells[i][j];
                cell.setCell(cellInfo.getColor(), cellInfo.getPoints());
            }
        }
    }

    private void pointToCell(int i, int j, Integer color) {
        Cell cell = cells[i][j];
        if (cell.color == 0) {
            cell.setCell(color, 1);
        } else {
            if (cell.points == 3) {
                int prevColor = cell.color;
                cell.setCell(0, 0);
                if (i - 1 >= 0) {
                    pointToCell(i - 1, j, prevColor);
                }

                if (i + 1 < cells.length) {
                    pointToCell(i + 1, j, prevColor);
                }
                if (j - 1 >= 0) {
                    pointToCell(i, j - 1, prevColor);
                }
                if (j + 1 < cells[0].length) {
                    pointToCell(i, j + 1, prevColor);
                }
            } else {
                cell.setCell(cell.color, cell.points + 1);
            }
        }

    }

    private static final class Cell {
        private final Group container;

        private int color;

        private int points;

        private Cell(Group container) {
            this.container = container;
        }

        public void setCell(int color, int point) {
            this.color = color;
            this.points = point;
            if (color == 0) {
                container.getChildren().get(0).setOpacity(0);
            } else {
                ImageView node = new ImageView(jouskas[color][point]);
                container.getChildren().set(0, node);
            }
        }
    }
}
