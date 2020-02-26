package com.github.tix320.jouska.client.infrastructure;


import com.github.tix320.jouska.core.game.JouskaGame;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;

public final class Context {

	private static final Publisher<JouskaGame> currentGame = Publisher.single();

	private Context() {
	}

	public static Observable<JouskaGame> currentGame() {
		return currentGame.asObservable();
	}

	public static void setCurrentGame(JouskaGame jouskaGame) {
		currentGame.publish(jouskaGame);
	}
}
