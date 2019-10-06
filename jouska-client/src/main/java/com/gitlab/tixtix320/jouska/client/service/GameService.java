package com.gitlab.tixtix320.jouska.client.service;

import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.sonder.api.common.rpc.Origin;

import java.util.List;

@Origin("game")
public interface GameService {

    @Origin("info")
    Observable<List<GameInfo>> getGames();

    @Origin("connect")
    Observable<String> connect(long gameId);

    @Origin("create")
    Observable<Long> create(GameInfo gameInfo);
}
