package com.github.tix320.jouska.core.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Player {
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

	public static Player[] getPlayers(int count) {
		List<Player> players = Arrays.asList(Player.values());
		Collections.shuffle(players);
		return players.subList(0, count).toArray(Player[]::new);
	}
}
