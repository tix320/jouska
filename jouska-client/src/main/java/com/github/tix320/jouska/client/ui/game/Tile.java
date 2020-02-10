package com.github.tix320.jouska.client.ui.game;

import com.github.tix320.jouska.client.ui.transtion.TransitionInterceptor;
import com.github.tix320.jouska.core.model.Player;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class Tile extends Region {


	private static final String[][] jouskas = new String[][]{
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/blue-jouska-2.png",
					"ui/game/blue-jouska-3.png",
					"ui/game/blue-jouska-4.png"
			},
			{
					"ui/game/green-jouska-1.png",
					"ui/game/green-jouska-2.png",
					"ui/game/green-jouska-3.png",
					"ui/game/green-jouska-4.png"
			},
			{
					"ui/game/red-jouska-1.png",
					"ui/game/red-jouska-2.png",
					"ui/game/red-jouska-3.png",
					"ui/game/red-jouska-4.png"
			},
			{
					"ui/game/yellow-jouska-1.png",
					"ui/game/yellow-jouska-2.png",
					"ui/game/yellow-jouska-3.png",
					"ui/game/yellow-jouska-4.png"
			},
	};

	private static final double ANIMATION_SECONDS = 0.4;

	private final ImageView content;

	public Tile() {
		this.content = new ImageView();
		getChildren().add(content);
	}

	public Node content() {
		return content;
	}

	public Transition disappearTransition() {
		return disappearTransition(Duration.seconds(ANIMATION_SECONDS));
	}

	public Transition appearTransition(Player player, int points) {
		Transition transition = appearTransition(Duration.seconds(ANIMATION_SECONDS));

		String imagePath = jouskas[player.ordinal()][points - 1];

		return TransitionInterceptor.intercept(transition, () -> content.setImage(new Image(imagePath)));
	}

	public Transition disAppearAndAppearTransition(Player player, int points) {
		String imagePath = jouskas[player.ordinal()][points - 1];
		Transition disappearTransition = disappearTransition(Duration.seconds(ANIMATION_SECONDS / 2));
		Transition appearTransition = TransitionInterceptor.intercept(
				appearTransition(Duration.seconds(ANIMATION_SECONDS / 2)),
				() -> content.setImage(new Image(imagePath)));

		return new SequentialTransition(disappearTransition, appearTransition);
	}

	private Transition appearTransition(Duration duration) {
		FadeTransition appearTransition = new FadeTransition(duration, content);
		appearTransition.setFromValue(0);
		appearTransition.setToValue(1);
		return appearTransition;
	}

	private Transition disappearTransition(Duration duration) {
		FadeTransition disappearTransition = new FadeTransition(duration, content);
		disappearTransition.setFromValue(1);
		disappearTransition.setToValue(0);
		return disappearTransition;
	}
}
