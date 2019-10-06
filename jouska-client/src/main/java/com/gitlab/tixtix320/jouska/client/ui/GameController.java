package com.gitlab.tixtix320.jouska.client.ui;

import com.gitlab.tixtix320.jouska.core.model.CellInfo;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class GameController implements Controller<GameBoard> {

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

    @Override
    public void initialize(GameBoard gameBoard) {
        fillBoard(gameBoard.getMatrix());
    }

    private void fillBoard(CellInfo[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                CellInfo cellInfo = board[i][j];
                int colorIndex = cellInfo.getColor();
                if (colorIndex == 0) {
                    gameGrid.add(new Rectangle(50, 50), j, i);
                    continue;
                }
                ImageView node = new ImageView(jouskas[colorIndex - 1][cellInfo.getPoints()]);
                String nodeId = i + "_" + j;
                node.setId(nodeId);
                node.setUserData(colorIndex);
                node.setOnMouseClicked(event -> {
                    if (true) { // my turn
                        int index = (Integer) node.getUserData() + 1;
                        if (index == jouskas[colorIndex - 1].length) {
                            index = 0;
                        }
                        node.setImage(new Image(jouskas[colorIndex - 1][index]));
                        node.setUserData(index);
                    }
                });
                gameGrid.add(node, j, i);
            }
        }
    }
}
