package com.gitlab.tixtix320.jouska.client.service;

import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.sonder.api.common.Origin;

import java.util.List;

@Origin("game")
public interface GameInfoService {

    @Origin("info")
    Observable<List<GameInfo>> getGames();

    @Origin("connect")
    Observable<Boolean> connect(int roomId);
}
