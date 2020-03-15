package com.github.tix320.jouska.client.infrastructure;

import com.github.tix320.jouska.core.model.Player;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class CurrentUserContext {

	private static SimpleObjectProperty<Player> player = new SimpleObjectProperty<>();

	public static Player getPlayer() {
		return player.get();
	}

	public static Property<Player> playerProperty() {
		return player;
	}

	public static void setPlayer(Player player) {
		CurrentUserContext.player.set(player);
	}
}
