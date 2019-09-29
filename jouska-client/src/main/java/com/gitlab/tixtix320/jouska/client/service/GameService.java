package com.gitlab.tixtix320.jouska.client.service;

import com.gitlab.tixtix320.jouska.core.model.CellInfo;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.sonder.api.common.Origin;

@Origin("game")
public interface GameService {

    @Origin("board")
    Observable<GameBoard> getBoard();

    @Origin("turn")
    Observable<Void> turn(int x, int y, CellInfo cellInfo);
}
