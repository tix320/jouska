package com.github.tix320.jouska.core.game;

import java.util.List;

public class CellChange {
	private final Point point;
	private final BoardCell boardCell;
	private final boolean collapse;
	private final List<CellChange> children;

	private CellChange() {
		this(null, null, false, null);
	}

	public CellChange(Point point, BoardCell boardCell, boolean collapse, List<CellChange> children) {
		this.point = point;
		this.boardCell = boardCell;
		this.collapse = collapse;
		this.children = children;
	}

	public Point getPoint() {
		return point;
	}

	public BoardCell getBoardCell() {
		return boardCell;
	}

	public boolean isCollapse() {
		return collapse;
	}

	public List<CellChange> getChildren() {
		return children;
	}
}

