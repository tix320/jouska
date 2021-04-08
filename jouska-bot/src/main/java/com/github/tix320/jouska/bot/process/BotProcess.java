package com.github.tix320.jouska.bot.process;

import java.io.*;

import com.github.tix320.deft.api.util.Shlex;
import com.github.tix320.jouska.core.application.game.BoardCell;
import com.github.tix320.jouska.core.application.game.PlayerColor;
import com.github.tix320.jouska.core.application.game.Point;
import com.github.tix320.jouska.core.application.game.ReadOnlyGameBoard;
import com.github.tix320.skimp.api.thread.LoopThread.BreakLoopException;
import com.github.tix320.skimp.api.thread.Threads;

/**
 * @author Tigran Sargsyan on 03-Apr-20.
 */
public class BotProcess {

	private static final String GAME_START_COMMAND = "START_GAME";
	private static final String GAME_END_COMMAND = "END_GAME";
	private static final String TURN_COMMAND = "TURN";

	private final BufferedReader reader;
	private final PrintWriter writer;

	public BotProcess(String processRunCommand) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder(Shlex.split(processRunCommand));
		Process process = processBuilder.start();
		System.out.println("Bot process started.");
		process.onExit().thenRunAsync(() -> System.out.println("Bot process exited."));
		Threads.createLoopDaemonThread(() -> {
			BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			try {
				String line = errorStream.readLine();
				if (line == null) {
					throw new BreakLoopException();
				}
				System.err.println("Bot error:" + line);
			} catch (IOException e) {
				e.printStackTrace();
				throw new BreakLoopException();
			}
		}).start();

		reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
	}

	public void startGame(int height, int width) {
		System.out.println("Game started.");
		writer.println(GAME_START_COMMAND);
		writer.println(height + " " + width);
	}

	public void endGame() {
		System.out.println("Game end.");
		writer.println(GAME_END_COMMAND);
	}

	public Point turn(ReadOnlyGameBoard board, PlayerColor myPlayer) throws IOException {
		System.out.println("Board received.");
		writer.println(TURN_COMMAND);

		StringBuilder boardString = new StringBuilder();
		for (int i = 0; i < board.getHeight(); i++) {
			StringBuilder row = new StringBuilder();
			for (int j = 0; j < board.getWidth(); j++) {
				BoardCell boardCell = board.get(i, j);
				PlayerColor player = boardCell.getColor();
				int points = boardCell.getPoints();
				if (player == null) {
					row.append(0).append(',').append(0);
				} else {
					if (player == myPlayer) {
						row.append(1);
					} else {
						row.append(2);
					}
					row.append(',').append(points);
				}
				row.append(" ");
			}
			row.deleteCharAt(row.length() - 1); // delete last space
			boardString.append(row).append('\n');
		}

		writer.print(boardString.toString());
		writer.flush();

		String turn = reader.readLine();
		String[] point = turn.split(":");
		int i = Integer.parseInt(point[0]);
		int j = Integer.parseInt(point[1]);
		return new Point(i, j);
	}
}
