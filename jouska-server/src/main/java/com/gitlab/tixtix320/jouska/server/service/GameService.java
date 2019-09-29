package com.gitlab.tixtix320.jouska.server.service;

import com.gitlab.tixtix320.sonder.api.common.Origin;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.sonder.api.common.extra.ClientID;

@Origin("game")
public interface GameService {

    @Origin("board")
    void sendBoard(GameBoard board, @ClientID long clientId);
}
