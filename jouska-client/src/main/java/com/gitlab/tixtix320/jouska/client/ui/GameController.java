package com.gitlab.tixtix320.jouska.client.ui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import com.gitlab.tixtix320.jouska.client.service.GameEndpoint;
import ui.model.CellInfo;

public class GameController implements Controller {

    private static final String[][] jouskas = new String[][]{
            {"ui/game/blue-jouska-1.png", "ui/game/blue-jouska-2.png", "ui/game/blue-jouska-3.png", "ui/game/blue-jouska-4.png"},
            {"ui/game/green-jouska-1.png", "ui/game/green-jouska-2.png", "ui/game/green-jouska-3.png", "ui/game/green-jouska-4.png"},
            {"ui/game/red-jouska-1.png", "ui/game/red-jouska-2.png", "ui/game/red-jouska-3.png", "ui/game/red-jouska-4.png"},
            {"ui/game/yellow-jouska-1.png", "ui/game/yellow-jouska-2.png", "ui/game/yellow-jouska-3.png", "ui/game/yellow-jouska-4.png"},
    };

    private final Scene scene;

    private final GridPane root;

    public GameController() {
        root = new GridPane();
        root.setGridLinesVisible(true);
        scene = new Scene(root);
    }

    @Override
    public Scene getOwnScene() {
        return scene;
    }

    @Override
    public void initialize() {
        GameEndpoint.boardState().subscribe(board -> fillBoard(board.getMatrix()));
    }

    private void fillBoard(CellInfo[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                CellInfo cellInfo = board[i][j];
                int colorIndex = cellInfo.getColor();
                ImageView node = new ImageView(jouskas[colorIndex][cellInfo.getPoints()]);
                String nodeId = i + "_" + j;
                node.setId(nodeId);
                node.setUserData(colorIndex);
                node.setOnMouseClicked(event -> {
                    if (true) { // my turn
                        int index = (Integer) node.getUserData() + 1;
                        if (index == jouskas[colorIndex].length) {
                            index = 0;
                        }
                        node.setImage(new Image(jouskas[colorIndex][index]));
                        node.setUserData(index);
                    }
                });
                root.add(node, j, i);
            }
        }
    }
}
