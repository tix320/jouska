package com.gitlab.tixtix320.jouska.client.service;

import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.subject.Subject;
import com.gitlab.tixtix320.sonder.api.common.Endpoint;


@Endpoint("game")
public class GameEndpoint {

    private static final Subject<GameBoard> boardState = Subject.single();

    @Endpoint("board")
    public void board(GameBoard board) {
        boardState.next(board);
    }

    public static Observable<GameBoard> boardState() {
        return boardState.asObservable();
    }
}
