package com.github.tix320.jouska.server.infrastructure.application.dbo;

import java.util.Set;

import com.github.tix320.jouska.core.application.game.BoardType;
import com.github.tix320.jouska.core.application.game.Game;
import com.github.tix320.jouska.core.application.game.creation.GameSettings;
import com.github.tix320.jouska.core.application.game.creation.RestorableGameSettings;
import com.github.tix320.jouska.core.model.Player;

/**
 * @author Tigran Sargsyan on 21-Apr-20.
 */
public final class DBGameSettings implements GameSettings {

	private final Player creator;

	private final Set<Player> accessedPlayers;

	private final RestorableGameSettings wrappedGameSettings;

	public DBGameSettings(Player creator, Set<Player> accessedPlayers, RestorableGameSettings wrappedGameSettings) {
		this.creator = creator;
		this.accessedPlayers = accessedPlayers;
		this.wrappedGameSettings = wrappedGameSettings;
	}

	public RestorableGameSettings getWrappedGameSettings() {
		return wrappedGameSettings;
	}

	@Override
	public String getName() {
		return wrappedGameSettings.getName();
	}

	@Override
	public BoardType getBoardType() {
		return wrappedGameSettings.getBoardType();
	}

	@Override
	public int getPlayersCount() {
		return wrappedGameSettings.getPlayersCount();
	}

	@Override
	public GameSettings changeName(String name) {
		return new DBGameSettings(creator, accessedPlayers, wrappedGameSettings.changeName(name));
	}

	@Override
	public GameSettings changePlayersCount(int playersCount) {
		return new DBGameSettings(creator, accessedPlayers, wrappedGameSettings.changePlayersCount(playersCount));
	}

	@Override
	public Game createGame() {
		return DBGame.createNew(creator, accessedPlayers, wrappedGameSettings);
	}
}
