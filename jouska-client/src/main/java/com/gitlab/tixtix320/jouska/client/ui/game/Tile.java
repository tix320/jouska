package com.gitlab.tixtix320.jouska.client.ui.game;

import com.gitlab.tixtix320.jouska.core.model.Player;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Tile extends Region {

	private static final String[][] jouskas = new String[][]{
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/blue-jouska-1.png",
					"ui/game/blue-jouska-2.png",
					"ui/game/blue-jouska-3.png",
					"ui/game/blue-jouska-4.png"
			},
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/blue-jouska-1.png",
					"ui/game/blue-jouska-2.png",
					"ui/game/blue-jouska-3.png",
					"ui/game/blue-jouska-4.png"
			},
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/green-jouska-1.png",
					"ui/game/green-jouska-2.png",
					"ui/game/green-jouska-3.png",
					"ui/game/green-jouska-4.png"
			},
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/red-jouska-1.png",
					"ui/game/red-jouska-2.png",
					"ui/game/red-jouska-3.png",
					"ui/game/red-jouska-4.png"
			},
			{
					"ui/game/blue-jouska-1.png",
					"ui/game/yellow-jouska-1.png",
					"ui/game/yellow-jouska-2.png",
					"ui/game/yellow-jouska-3.png",
					"ui/game/yellow-jouska-4.png"
			},
	};

	public Player player;

	public int points;

	public Tile() {
		Circle e = new Circle(4);
		e.setOpacity(0);
		getChildren().add(e);
	}

	public Transition changeContent(Player player, int points) {
		Transition transition;
		if (player == Player.NONE || points == 0) {
			transition = disappearTransition();
			this.player = Player.NONE;
			this.points = 0;
		}
		else {
			ImageView imageView = new ImageView(jouskas[player.ordinal()][Math.min(4, points)]);

			if (this.points == 0) {
				getChildren().set(0, imageView);
				transition = appearTransition(imageView);
			}
			else {
				Transition disappearTransition = disappearTransition();
				disappearTransition.setOnFinished(actionEvent -> getChildren().set(0, imageView));

				transition = new SequentialTransition(disappearTransition, appearTransition(imageView));
			}
			this.player = player;
			this.points = points;
		}
		return transition;
	}

	private Transition appearTransition(Node node) {
		FadeTransition appearTransition = new FadeTransition(Duration.seconds(0.3), node);
		appearTransition.setFromValue(0);
		appearTransition.setToValue(1);
		return appearTransition;
	}

	private Transition disappearTransition() {
		Node node = getChildren().get(0);
		FadeTransition disappearTransition = new FadeTransition(Duration.seconds(0.3), node);
		disappearTransition.setFromValue(1);
		disappearTransition.setToValue(0);
		return disappearTransition;
	}
}
