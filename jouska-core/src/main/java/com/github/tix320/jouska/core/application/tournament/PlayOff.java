package com.github.tix320.jouska.core.application.tournament;

import java.util.List;
import java.util.Optional;

import com.github.tix320.jouska.core.application.game.GameWithSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.util.None;

public interface PlayOff {

	List<Player> getPlayers();

	List<List<PlayOffGame>> getGamesStructure();

	Observable<GameWithSettings> createdGames();

	MonoObservable<None> completed();

	Optional<Player> getWinner();
}
