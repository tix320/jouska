package com.gitlab.tixtix320.jouska.client.ui.game;

import java.util.List;

import com.gitlab.tixtix320.jouska.core.model.Player;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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

	public Player color;

	public int points;

	public Tile() {
		Circle e = new Circle(4);
		e.setOpacity(0);
		getChildren().add(e);
	}

	public List<KeyFrame> changeContent(Player color, int point) {
		this.color = color;
		this.points = point;
		if (color == Player.NONE) {
			Node node = getChildren().get(0);
			KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), new KeyValue(node.opacityProperty(), 0));

			return List.of(keyFrame);
		}
		else {
			Node node = getChildren().get(0);
			KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), new KeyValue(node.opacityProperty(), 0));

			ImageView imageView = new ImageView(jouskas[color.ordinal()][point]);
			imageView.setOpacity(0);
			getChildren().set(0, imageView);
			KeyFrame keyFrame2 = new KeyFrame(Duration.seconds(1.0), new KeyValue(imageView.opacityProperty(), 1));

			return List.of(keyFrame, keyFrame2);
		}
	}
}
