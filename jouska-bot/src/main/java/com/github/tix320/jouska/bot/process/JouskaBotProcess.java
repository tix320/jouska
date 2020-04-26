package com.github.tix320.jouska.bot.process;

import com.github.tix320.jouska.core.application.game.BoardCell;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.Point;

/**
 * @author Tigran Sargsyan on 03-Apr-20.
 */
public class JouskaBotProcess {

	private static final String GAME_START_COMMAND = "START_GAME";
	private static final String GAME_END_COMMAND = "END_GAME";
	private static final String TURN_COMMAND = "TURN";

	private final ProcessCommunicator processCommunicator;

	public JouskaBotProcess(String processRunCommand) {
		this.processCommunicator = new ProcessCommunicator(processRunCommand);
	}

	public void startGame() {
		System.out.println("Start game");
		processCommunicator.write(GAME_START_COMMAND);
	}

	public void endGame() {
		System.out.println("End game");
		processCommunicator.write(GAME_END_COMMAND);
	}

	public Point turn(BoardCell[][] board, PlayerColor myPlayer) {
		System.out.println("Send turn");
		processCommunicator.write(TURN_COMMAND);

		StringBuilder boardString = new StringBuilder();
		for (BoardCell[] boardCells : board) {
			StringBuilder row = new StringBuilder();
			for (BoardCell boardCell : boardCells) {
				PlayerColor player = boardCell.getColor();
				int points = boardCell.getPoints();
				if (player == null) {
					row.append(0).append(',').append(0);
				}
				else {
					if (player == myPlayer) {
						row.append(1);
					}
					else {
						row.append(2);
					}
					row.append(',').append(points);
				}
			}
			boardString.append(row).append('\n');
		}
		processCommunicator.write(boardString.toString());

		String turn = processCommunicator.readLine();
		String[] point = turn.split(":");
		int i = Integer.parseInt(point[0]);
		int j = Integer.parseInt(point[1]);
		return new Point(i, j);
	}
}
