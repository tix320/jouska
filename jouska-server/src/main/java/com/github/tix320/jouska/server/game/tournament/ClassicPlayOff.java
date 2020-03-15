package com.github.tix320.jouska.server.game.tournament;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.game.Game;
import com.github.tix320.jouska.core.game.GameFactory;
import com.github.tix320.jouska.core.model.GameSettings;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.reactive.observable.Observable;

public class ClassicPlayOff implements PlayOff {

	private final GameSettings gameSettings;

	private final List<Player> waitingPlayers;

	public ClassicPlayOff(GameSettings gameSettings, List<Player> players) {
		if (players.size() < 2) {
			throw new IllegalArgumentException("Players count must be >=2");
		}

		int playersCount = gameSettings.getPlayersCount();
		if (playersCount != 2) {
			throw new IllegalStateException(
					String.format("Invalid players count %s. Classic group game must be for two players",
							playersCount));
		}

		this.gameSettings = gameSettings;
		this.waitingPlayers = players;
	}

	@Override
	public Set<Player> getWaitingPlayers() {
		return null;
	}

	@Override
	public Observable<List<Game>> games() {
		return null;
	}

	private List<Game> assembleNextTour(List<Player> waitingPlayers) {
		Collections.shuffle(waitingPlayers);

		List<Game> games = new ArrayList<>();
		while (!waitingPlayers.isEmpty()) {
			int lastIndex = waitingPlayers.size() - 1;
			if (waitingPlayers.size() > 1) {
				Player player1 = removeAndGet(waitingPlayers, lastIndex);
				Player player2 = removeAndGet(waitingPlayers, lastIndex - 1);

				games.add(GameFactory.create(gameSettings, Set.of(player1, player2)));
			}
			else {
				break;
			}
		}
		return games;
	}

	private static <T> T removeAndGet(List<T> list, int index) {
		T value = list.get(index);
		list.remove(index);
		return value;
	}

}
