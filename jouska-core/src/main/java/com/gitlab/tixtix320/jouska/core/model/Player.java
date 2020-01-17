package com.gitlab.tixtix320.jouska.core.model;

public enum Player {
	NONE {
		@Override
		public String getColorCode() {
			throw new IllegalStateException();
		}
	},
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
