package com.github.tix320.jouska.client.ui.game;

import java.util.Objects;

public final class Point {
	int i;
	int j;

	public Point(int i, int j) {
		this.i = i;
		this.j = j;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Point point = (Point) o;
		return i == point.i && j == point.j;
	}

	@Override
	public int hashCode() {
		return Objects.hash(i, j);
	}
}
