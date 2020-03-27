package com.github.tix320.jouska.core.application.tournament;

import java.util.List;
import java.util.Map;

import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;

public interface Group {

	List<Player> getPlayers();

	List<Game> games();

	ReadOnlyProperty<Map<Player, Integer>> points();

	ReadOnlyProperty<Boolean> completed();
}
