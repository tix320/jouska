package com.github.tix320.jouska.core.application.game;

import java.util.Objects;

public final class Point {
	public final int i;
	public final int j;

	private Point() {
		this(-1, -1);
	}

	public Point(int i, int j) {
		this.i = i;
		this.j = j;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
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

	@Override
	public String toString() {
		return i + ":" + j;
	}
}
