package com.github.tix320.jouska.server.game.tournament;

import java.util.List;
import java.util.Map;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.property.ReadOnlyProperty;
import com.github.tix320.kiwi.api.util.None;

public interface Group {

	List<Player> getPlayers();

	List<Game> games();

	ReadOnlyProperty<Map<Player, Integer>> points();

	ReadOnlyProperty<Boolean> completed();
}
