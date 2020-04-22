package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 19-Apr-20.
 */
public class GamePlayersOfflineWarning {

	private final String gameName;

	private final List<Player> players;

	private GamePlayersOfflineWarning() {
		this(null, null);
	}

	public GamePlayersOfflineWarning(String gameName, List<Player> players) {
		this.gameName = gameName;
		this.players = players;
	}

	public String getGameName() {
		return gameName;
	}

	public List<Player> getPlayers() {
		return players;
	}
}
