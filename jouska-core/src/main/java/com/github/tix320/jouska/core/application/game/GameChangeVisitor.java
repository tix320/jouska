package com.github.tix320.jouska.core.application.game;

/**
 * @author Tigran Sargsyan on 22-Apr-20.
 */
public interface GameChangeVisitor {

	void visit(PlayerTurn playerTurn);

	void visit(PlayerTimedTurn playerTimedTurn);

	void visit(PlayerKick playerKick);

	void visit(GameComplete gameComplete);
}
