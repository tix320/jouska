package com.gitlab.tixtix320.jouska.core.model;

public enum Player {
	NONE,
	BLUE,
	GREEN,
	RED,
	YELLOW;

	public static Player fromNumber(int number) {
		switch (number) {
			case 0:
				return NONE;
			case 1:
				return BLUE;
			case 2:
				return GREEN;
			case 3:
				return RED;
			case 4:
				return YELLOW;
			default:
				throw new IllegalArgumentException("pfff");
		}
	}
}
