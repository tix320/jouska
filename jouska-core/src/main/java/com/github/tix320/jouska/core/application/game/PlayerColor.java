package com.github.tix320.jouska.core.application.game;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PlayerColor {
	BLUE {
		@Override
		public String getColorCode() {
			return "#3498DB";
		}
	},
	GREEN {
		@Override
		public String getColorCode() {
			return "#2ECC71";
		}
	},
	RED {
		@Override
		public String getColorCode() {
			return "#E74C3C";
		}
	},
	YELLOW {
		@Override
		public String getColorCode() {
			return "#F1C40F";
		}
	};

	public abstract String getColorCode();

	public static PlayerColor[] random(int count) {
		List<PlayerColor> players = Arrays.asList(PlayerColor.values());
		if (count > players.size()) {
			throw new IllegalArgumentException("Count is too long");
		}
		Collections.shuffle(players);
		return players.subList(0, count).toArray(PlayerColor[]::new);
	}
}
