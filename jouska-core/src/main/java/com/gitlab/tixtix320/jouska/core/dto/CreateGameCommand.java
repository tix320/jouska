package com.gitlab.tixtix320.jouska.core.dto;

public final class CreateGameCommand {

	private final String name;

	private final int playersCount;

	private CreateGameCommand() {
		this("auu", 1);
	}

	public CreateGameCommand(String name, int playersCount) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Empty name");
		}
		if (!(playersCount > 0 && playersCount <= 4)) {
			throw new IllegalArgumentException(String.format("Invalid players count %s", playersCount));
		}

		this.name = name;
		this.playersCount = playersCount;
	}

	public String getName() {
		return name;
	}

	public int getPlayersCount() {
		return playersCount;
	}
}
