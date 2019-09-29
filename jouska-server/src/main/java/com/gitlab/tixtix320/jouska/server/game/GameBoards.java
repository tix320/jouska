package com.gitlab.tixtix320.jouska.server.game;

import com.gitlab.tixtix320.jouska.core.model.CellInfo;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;

public final class GameBoards {

    public static GameBoard defaultBoard(int players) {
        int width = 12;
        int height = 6;
        CellInfo[][] cellInfos = new CellInfo[height][width];
        for (int i = 0; i < cellInfos.length; i++) {
            for (int j = 0; j < cellInfos[i].length; j++) {
                cellInfos[i][j] = new CellInfo(-1, -1);
            }
        }

        int penultRowIndex;
        CellInfo[] penultRow;
        switch (players) {
            case 2:
                cellInfos[1][1] = new CellInfo(1, 3);
                penultRowIndex = cellInfos.length - 2;
                penultRow = cellInfos[penultRowIndex];
                cellInfos[penultRowIndex][penultRow.length - 2] = new CellInfo(2, 3);
                break;
            case 3:
                cellInfos[1][1] = new CellInfo(1, 3);
                penultRowIndex = cellInfos.length - 2;
                penultRow = cellInfos[penultRowIndex];
                cellInfos[penultRowIndex][penultRow.length - 2] = new CellInfo(2, 3);
                cellInfos[penultRowIndex][1] = new CellInfo(3, 3);
                break;
            case 4:
                cellInfos[1][1] = new CellInfo(1, 3);
                penultRowIndex = cellInfos.length - 2;
                penultRow = cellInfos[penultRowIndex];
                cellInfos[penultRowIndex][penultRow.length - 2] = new CellInfo(2, 3);
                cellInfos[penultRowIndex][1] = new CellInfo(3, 3);
                cellInfos[1][penultRowIndex] = new CellInfo(4, 3);
                break;
            default:
                throw new IllegalArgumentException(String.format("Players must be 2,3,4, but was %s", players));
        }

        return new GameBoard(cellInfos);
    }
}
