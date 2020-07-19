package com.github.tix320.jouska.bot.process;

import com.github.tix320.jouska.core.application.game.BoardCell;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.jouska.core.application.game.ReadOnlyGameBoard;

/**
 * @author Tigran Sargsyan on 03-Apr-20.
 */
public class BotProcess {

	private static final String GAME_START_COMMAND = "START_GAME";
	private static final String GAME_END_COMMAND = "END_GAME";
	private static final String TURN_COMMAND = "TURN";

	private final SubProcess subProcess;

	public BotProcess(String processRunCommand) {
		this.subProcess = new SubProcess(processRunCommand);
	}

	public void startGame(int height, int width) {
		System.out.println("To Bot Process: Start game");
		subProcess.writeLn(GAME_START_COMMAND);
		subProcess.writeLn(height + " " + width);
	}

	public void endGame() {
		System.out.println("To Bot Process: End game");
		subProcess.writeLn(GAME_END_COMMAND);
	}

	public Point turn(ReadOnlyGameBoard board, PlayerColor myPlayer) {
		System.out.println("To Bot Process: Send board");
		subProcess.writeLn(TURN_COMMAND);

		StringBuilder boardString = new StringBuilder();
		for (int i = 0; i < board.getHeight(); i++) {
			StringBuilder row = new StringBuilder();
			for (int j = 0; j < board.getWidth(); j++) {
				BoardCell boardCell = board.get(i, j);
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
				row.append(" ");
			}
			row.deleteCharAt(row.length() - 1); // delete last space
			boardString.append(row).append('\n');
		}

		subProcess.write(boardString.toString());

		String turn = subProcess.readLine();
		String[] point = turn.split(":");
		int i = Integer.parseInt(point[0]);
		int j = Integer.parseInt(point[1]);
		return new Point(i, j);
	}
}
